package org.esa.beam.visat.toolviews.layermanager.layersrc.image;

import com.bc.ceres.binding.PropertyContainer;
import com.bc.ceres.glayer.Layer;
import com.bc.ceres.glayer.LayerType;
import com.bc.ceres.glayer.LayerTypeRegistry;
import com.bc.ceres.glayer.support.ImageLayer;
import com.bc.ceres.glevel.MultiLevelSource;
import com.bc.ceres.glevel.support.DefaultMultiLevelModel;
import com.bc.ceres.glevel.support.DefaultMultiLevelSource;
import com.bc.ceres.grender.Rendering;

import javax.media.jai.operator.FileLoadDescriptor;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.io.File;

// todo - remove this class, code here belongs to ImageFileLayerType, which shall simply create an ImageLayer (nf,mp 10.2009)
class ImageFileLayer extends Layer {

    private ImageLayer layerDelegate;

    ImageFileLayer(LayerType layerType, PropertyContainer configuration) {
        super(layerType, configuration);
        final MultiLevelSource multiLevelSource = createMultiLevelSource();

        final LayerType imageLayerType = LayerTypeRegistry.getLayerType(ImageLayer.Type.class.getName());
        final PropertyContainer template = imageLayerType.createLayerConfig(null); // todo - pass correct context
        template.setValue(ImageLayer.PROPERTY_NAME_MULTI_LEVEL_SOURCE, multiLevelSource);
        template.setValue(ImageLayer.PROPERTY_NAME_IMAGE_TO_MODEL_TRANSFORM,
                          multiLevelSource.getModel().getImageToModelTransform(0));
        template.setValue(ImageLayer.PROPERTY_NAME_BORDER_SHOWN, false);
        layerDelegate = new ImageLayer((ImageLayer.Type) imageLayerType, template);
        setName(getName());
    }

    @Override
    protected void renderLayer(Rendering rendering) {
        layerDelegate.render(rendering);

    }

    @Override
    protected Rectangle2D getLayerModelBounds() {
        return layerDelegate.getModelBounds();
    }

    @Override
    protected void disposeLayer() {
        layerDelegate.dispose();
    }

    @Override
    public void regenerate() {
        layerDelegate.regenerate();
    }

    private MultiLevelSource createMultiLevelSource() {
        final PropertyContainer configuration = getConfiguration();
        RenderedImage image = (RenderedImage) configuration.getValue(ImageFileLayerType.PROPERTY_NAME_IMAGE);
        if (image == null) {
            final File file = (File) configuration.getValue(ImageFileLayerType.PROPERTY_NAME_IMAGE_FILE);
            image = FileLoadDescriptor.create(file.getPath(), null, true, null);
        }
        final AffineTransform transform = (AffineTransform) configuration.getValue(
                ImageFileLayerType.PROPERTY_NAME_WORLD_TRANSFORM);
        final Rectangle2D modelBounds = DefaultMultiLevelModel.getModelBounds(transform, image);
        return new DefaultMultiLevelSource(image, new DefaultMultiLevelModel(1, transform, modelBounds));
    }

}
