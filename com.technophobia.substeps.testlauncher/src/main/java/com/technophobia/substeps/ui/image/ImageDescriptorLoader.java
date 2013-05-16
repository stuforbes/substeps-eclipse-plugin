package com.technophobia.substeps.ui.image;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;

import com.technophobia.substeps.FeatureRunnerPlugin;
import com.technophobia.substeps.junit.ui.SubstepsIcon;
import com.technophobia.substeps.supplier.Transformer;

public class ImageDescriptorLoader implements Transformer<SubstepsIcon, ImageDescriptor> {

    private static final String ICON_PREFIX_PATH = "icons/full";


    @Override
    public ImageDescriptor from(final SubstepsIcon icon) {
        final URL url = FileLocator.find(FeatureRunnerPlugin.instance().getBundle(), new Path(ICON_PREFIX_PATH + "/"
                + icon.getPath()), null);
        if (url != null) {
            return ImageDescriptor.createFromURL(url);
        }

        FeatureRunnerPlugin.error("Could not find url for icon " + icon);
        return null;
    }

}
