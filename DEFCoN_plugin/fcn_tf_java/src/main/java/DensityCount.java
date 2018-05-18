import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.LutLoader;
import ij.plugin.filter.PlugInFilter;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.LUT;
import ij.WindowManager;
import ij.ImageStack;

import net.imagej.tensorflow.Tensors;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import org.tensorflow.*;


public class DensityCount implements PlugInFilter {
    private ImagePlus image;
    private ResultsTable rt;
    private Session tfSession;
    private Roi roi;
    private ImageStack densityStack;

    // Runs DEFCoN on the selected image stack. It outputs a results table with the count on each frame, and a stack of
    // the predicted density maps
    public void run(ImageProcessor ip) {
        int stack_size = image.getImageStackSize();
        roi = WindowManager.getCurrentImage().getRoi();
        Roi reshapedRoi = initRoiAndStack();

        for (int i=1; i <= stack_size; i++ ) {
            ImageProcessor proc = image.getImageStack().getProcessor(i);
            ImagePlus slice = new ImagePlus("DEFCoN", proc);

            rt.incrementCounter();
            // Make the prediction
            float prediction = predict(slice, reshapedRoi);
            // Build the results table
            int pred_int = Math.round(prediction);
            rt.addValue("Rounded count", pred_int);
            rt.addValue("Exact count", prediction);
            IJ.showProgress(i, stack_size);
        }

        // Display the stack of density maps with viridis colormap
        ImagePlus density_image = new ImagePlus("Density map", densityStack);
        LUT viridis = LutLoader.openLut(IJ.getDir("plugins") + "/DEFCoN/mpl-viridis.lut");
        density_image.setLut(viridis);
        density_image.show();

        rt.show("Fluorophore count");
    }

    public int setup(String arg, ImagePlus imp) {
        // Unlock the image
        if (imp.isLocked()) {imp.unlock();}
        image = imp;

        // Loading DEFCoN tensorflow model
        String pluginDir = IJ.getDir("plugins");
        SavedModelBundle smb = SavedModelBundle.load(pluginDir + "/DEFCoN/tf_density_count", "serve");
        tfSession = smb.session();

        // Create the results table
        rt = new ResultsTable();

        // Only accepts 8bit and 16bit images.
        return DOES_8G | DOES_16;
    }

    // Gets the current ROI in ImageJ and crops it so that each dimension is divisible by 4
    private Roi initRoiAndStack() {
        int image_width = image.getWidth();
        int image_height = image.getHeight();

        if (roi == null) {
            roi = new Roi(0,0,image_width,image_height);
        }
        Roi reshapedRoi = new Roi(roi.getBounds().x,
                roi.getBounds().y,
                roi.getBounds().width - roi.getBounds().width % 4,
                roi.getBounds().height - roi.getBounds().height % 4);

        WindowManager.getCurrentImage().setRoi(reshapedRoi);
        densityStack = new ImageStack(reshapedRoi.getBounds().width, reshapedRoi.getBounds().height);
        return reshapedRoi;
    }

    private <T extends RealType<T>> float predict(final ImagePlus imp, Roi reshapedRoi) {

        // Crop the image to the ROI
        imp.setRoi(reshapedRoi);
        ImagePlus impRoi = imp.crop();
        int width = impRoi.getWidth();
        int height = impRoi.getHeight();

        // Converts the ImagePlus input to a tensorflow tensor
        Tensor<Float> inputTensor = imageToTensor(impRoi);

        // Make the prediction with DEFCoN
        Tensor<Float> outputTensor = tfSession.runner()
                .feed("input_tensor", inputTensor)
                .fetch("output_tensor")
                .run().get(0).expect(Float.class);

        // Transforms the predicted tensor into a (1, height, width, 1) float array
        float[][][][] pred = outputTensor.copyTo(new float[1][height][width][1]);

        // Creates a FloatProcessor for the output density map image
        FloatProcessor ip_density = new FloatProcessor(width, height);

        // Assigns the pixels of the density map image, and sums the pixels to produce the global count
        float count = 0;
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                count += pred[0][y][x][0];
                ip_density.setf(x, y, pred[0][y][x][0]);
            }
        }
        // Adds the density image to the displayed density stack
        densityStack.addSlice(ip_density);

        return count;
    }

    protected static Tensor<Float> imageToTensor(final ImagePlus imagePlus) {

        // Convert to float Img (imglib2)
        Img<FloatType> img = ImageJFunctions.convertFloat(imagePlus);
        // All values between 0 and 1 (for int16). Not that important, since DEFCoN normalizes input directly
        // Img<FloatType> img_divided = divide(img, 65535);

        // Creates a tensorflow tensor from the Img
        // Second argument: ImageJ image dimensions are in XY order, tensorflow expects YX
        Tensor<Float> imageTensor = Tensors.tensor(img, new int[]{1,0});

        // All this section is there to add two dimensions to imageTensor, to be consistent with tensorflow layer input
        // shape(imageTensor) = (height, width) -> shape(inputTensor) = (1, height, width, 1)
        Graph graph = new Graph();
        GraphBuilder builder = new GraphBuilder(graph);

        Output imageTensorOutput = graph.opBuilder("Const", "tensor_image")
                .setAttr("dtype", imageTensor.dataType())
                .setAttr("value", imageTensor)
                .build().output(0);
        Output<Float> expandedTensorOutput = builder.expandDims("dim-1",
                builder.expandDims("dim0", imageTensorOutput, builder.constant("make_batch", 0)),
                builder.constant("make_channel", -1));

        Session s0 = new Session(graph);
        Tensor<Float> inputTensor = s0.runner().fetch(expandedTensorOutput.op().name()).run().get(0).expect(Float.class);

        return inputTensor;
    }

    protected static class GraphBuilder {
        // A tensorflow builder with two methods. "constant" builds an Output with a single scalar value.
        // "expandDims" adds a dimension to "input", at the position "dim" (with numpy/tensorflow syntax: 0 for the
        // first position, -1 in the end)

        GraphBuilder(Graph g) {
            this.g = g;
        }

        private Graph g;

        <T> Output<T> constant(String name, Object value, Class<T> type) {
            Tensor<T> t = Tensor.create(value, type);
            return g.opBuilder("Const", name)
                    .setAttr("dtype", DataType.fromClass(type))
                    .setAttr("value", t)
                    .build()
                    .output(0);
        }
        Output<Integer> constant(String name, int value) {
            return this.constant(name, value, Integer.class);
        }

        <T> Output<T> expandDims(String name, Output<T> input, Output<Integer> dim) {
            return g.opBuilder("ExpandDims", name).addInput(input).addInput(dim).build().output(0);
        }
    }

    private static <T extends RealType<T>> Img<T> divide(Img<T> img, double max_value) {
        // Divide every pixel value in an Img by "max_value".

        for ( T pixel : img )
        {
            double value = pixel.getRealDouble();
            double new_value = value/max_value;
            pixel.setReal(new_value);
        }
        return img;
    }

}

