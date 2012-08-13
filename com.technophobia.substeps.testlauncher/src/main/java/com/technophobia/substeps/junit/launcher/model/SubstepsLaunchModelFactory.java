package com.technophobia.substeps.junit.launcher.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;

import com.technophobia.eclipse.launcher.config.SubstepsLaunchConfigurationConstants;
import com.technophobia.substeps.FeatureRunnerPlugin;
import com.technophobia.substeps.junit.launcher.SubstepsFeatureLaunchShortcut;
import com.technophobia.substeps.supplier.Transformer;

public class SubstepsLaunchModelFactory implements LaunchModelFactory {

    private final Transformer<IProject, String> substepsFolderLocator;


    public SubstepsLaunchModelFactory(final Transformer<IProject, String> substepsFolderLocator) {
        this.substepsFolderLocator = substepsFolderLocator;
    }


    @Override
    public LaunchModel createFrom(final IResource resource) {
        final IProject project = resource.getProject();
        final String filePath = resource.getFullPath().removeFirstSegments(1).toOSString();
        final String substepsFolder = substepsFolderLocator.from(project);

        return new SubstepsLaunchModel(project.getName(), filePath, substepsFolder, Collections.<String> emptyList());
    }


    @Override
    public LaunchModel createFrom(final ILaunchConfiguration config) {
        final String projectName = getConfigAttribute(config, SubstepsLaunchConfigurationConstants.ATTR_FEATURE_PROJECT);
        final String featureFile = getConfigAttribute(config, SubstepsFeatureLaunchShortcut.ATTR_FEATURE_FILE);
        final String substepsFile = getConfigAttribute(config, SubstepsLaunchConfigurationConstants.ATTR_SUBSTEPS_FILE);

        final Collection<String> beforeAndAfterProcessors = beforeAndAfterProcessorsFrom(config);
        return new SubstepsLaunchModel(projectName, featureFile, substepsFile, beforeAndAfterProcessors);
    }


    private Collection<String> beforeAndAfterProcessorsFrom(final ILaunchConfiguration config) {
        final String beforeAndAfterProcessorsStr = getConfigAttribute(config,
                SubstepsLaunchConfigurationConstants.ATTR_BEFORE_AND_AFTER_PROCESSORS);
        final String[] split = beforeAndAfterProcessorsStr.split(";");
        final Collection<String> beforeAndAfterProcessors = Arrays.asList(split);
        return beforeAndAfterProcessors;
    }


    private String getConfigAttribute(final ILaunchConfiguration config, final String configName) {
        try {
            return config.getAttribute(configName, "");
        } catch (final CoreException e) {
            FeatureRunnerPlugin.log(e);
            return "";
        }
    }
}
