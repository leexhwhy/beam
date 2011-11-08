/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package org.esa.beam.glayer;

import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.glayer.Layer;
import com.bc.ceres.glayer.LayerContext;
import com.bc.ceres.glayer.LayerType;
import com.bc.ceres.glayer.LayerTypeRegistry;
import com.bc.ceres.glayer.support.ImageLayer;
import org.esa.beam.framework.datamodel.BitmaskDef;
import org.esa.beam.framework.datamodel.BitmaskOverlayInfo;
import org.esa.beam.framework.datamodel.Mask;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.RasterDataNode;

import java.awt.geom.AffineTransform;

/**
 * @deprecated since BEAM 4.7, replaced by MaskLayerType
 */
@Deprecated
public class BitmaskLayerType extends ImageLayer.Type {

    public static final String PROPERTY_NAME_BITMASK_DEF = "bitmaskDef";
    public static final String PROPERTY_NAME_PRODUCT = "product";
    
    private static final String TYPE_NAME = "BitmaskLayerType";
    private static final String[] ALIASES = {"org.esa.beam.glayer.BitmaskLayerType"};

    public static Layer createBitmaskLayer(RasterDataNode raster, final BitmaskDef bitmaskDef,
                                           AffineTransform i2mTransform) {
        final LayerType type = LayerTypeRegistry.getLayerType(BitmaskLayerType.class);
        final PropertySet configuration = type.createLayerConfig(null);
        configuration.setValue(BitmaskLayerType.PROPERTY_NAME_BITMASK_DEF, bitmaskDef);
        configuration.setValue(BitmaskLayerType.PROPERTY_NAME_PRODUCT, raster.getProduct());
        final Layer layer = type.createLayer(null, configuration);
        final BitmaskOverlayInfo overlayInfo = raster.getBitmaskOverlayInfo();
        layer.setVisible(overlayInfo != null && overlayInfo.containsBitmaskDef(bitmaskDef));
        return layer;
    }

    @Override
    public String getName() {
        return TYPE_NAME;
    }
    
    @Override
    public String[] getAliases() {
        return ALIASES;
    }

    @Override
    public Layer createLayer(LayerContext ctx, PropertySet configuration) {
        final Product product = (Product) configuration.getValue(PROPERTY_NAME_PRODUCT);
        final BitmaskDef bitmaskDef = (BitmaskDef) configuration.getValue(PROPERTY_NAME_BITMASK_DEF);
        String maskName = bitmaskDef.getName();
        Mask mask = product.getMaskGroup().get(maskName);
        if (mask == null) {
            throw new IllegalArgumentException("Mask '" + maskName + "'not available in product.");
        }
        
        MaskLayerType maskLayerType = LayerTypeRegistry.getLayerType(MaskLayerType.class);
        PropertySet maskConfiguration = maskLayerType.createLayerConfig(null);
        for (Property property : maskConfiguration.getProperties()) {
            String propertyName = property.getName();
            Property srcProperty = configuration.getProperty(propertyName);
            if (srcProperty != null) {
                maskConfiguration.setValue(propertyName, srcProperty.getValue());
            }
        }
        maskConfiguration.setValue(MaskLayerType.PROPERTY_NAME_MASK, mask);
        return maskLayerType.createLayer(ctx, maskConfiguration);
    }

    @Override
    public PropertySet createLayerConfig(LayerContext ctx) {
        final PropertySet vc = super.createLayerConfig(ctx);

        vc.addProperty(Property.create(PROPERTY_NAME_BITMASK_DEF, BitmaskDef.class));
        vc.getProperty(PROPERTY_NAME_BITMASK_DEF).getDescriptor().setNotNull(true);

        vc.addProperty(Property.create(PROPERTY_NAME_PRODUCT, Product.class));
        vc.getProperty(PROPERTY_NAME_PRODUCT).getDescriptor().setNotNull(true);

        return vc;
    }

    
    public Layer createLayer(BitmaskDef bitmaskDef, Product product, AffineTransform i2m) {
        final PropertySet configuration = createLayerConfig(null);
        configuration.setValue(PROPERTY_NAME_BITMASK_DEF, bitmaskDef);
        configuration.setValue(PROPERTY_NAME_PRODUCT, product);
        return createLayer(null, configuration);
    }


}