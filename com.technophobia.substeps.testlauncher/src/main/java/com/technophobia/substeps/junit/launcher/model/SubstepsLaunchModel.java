package com.technophobia.substeps.junit.launcher.model;

import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

import com.technophobia.eclipse.launcher.config.SubstepsLaunchConfigurationConstants;
import com.technophobia.substeps.junit.launcher.SubstepsFeatureLaunchShortcut;
import com.technophobia.substeps.junit.launcher.config.SubstepsLaunchConfigWorkingCopyDecorator;

public class SubstepsLaunchModel implements LaunchModel {

    private String projectName;
    private String featureFile;
    private String substepsFile;
    private Collection<String> beforeAndAfterProcessors;


    public SubstepsLaunchModel() {
        // Default constructor
    }


    public SubstepsLaunchModel(final String projectName, final String featureFile, final String substepsFile,
            final Collection<String> beforeAndAfterProcessors) {
        this.projectName = projectName;
        this.featureFile = featureFile;
        this.substepsFile = substepsFile;
        this.beforeAndAfterProcessors = beforeAndAfterProcessors;
    }


    @Override
    public void saveTo(final ILaunchConfigurationWorkingCopy config) {
        config.setAttribute(SubstepsLaunchConfigurationConstants.ATTR_FEATURE_PROJECT, projectName);
        config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, projectName);

        config.setAttribute(SubstepsFeatureLaunchShortcut.ATTR_FEATURE_FILE, featureFile);

        // config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS,
        // vmArgs(featureFile, project()));

        // final Collection<String> stepImplementationClasses =
        // FeatureEditorPlugin.instance()
        // .getStepImplementationProvider().stepImplementationClasses(project());
        // config.setAttribute(SubstepsLaunchConfigurationConstants.ATTR_STEP_IMPLEMENTATION_CLASSES,
        // createStringFrom(stepImplementationClasses));

        config.setAttribute(SubstepsLaunchConfigurationConstants.ATTR_SUBSTEPS_FILE, substepsFile);

        config.setAttribute(SubstepsLaunchConfigurationConstants.ATTR_BEFORE_AND_AFTER_PROCESSORS,
                createStringFrom(beforeAndAfterProcessors));

        config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME,
                SubstepsLaunchConfigWorkingCopyDecorator.FEATURE_TEST);
        config.setAttribute(SubstepsLaunchConfigurationConstants.ATTR_KEEPRUNNING, false);
        config.setAttribute(SubstepsLaunchConfigurationConstants.ATTR_TEST_CONTAINER, "");
        config.setAttribute(SubstepsLaunchConfigurationConstants.ATTR_TEST_RUNNER_KIND,
                SubstepsLaunchConfigurationConstants.JUNIT4_TEST_KIND_ID);
    }


    public String getProjectName() {
        return projectName;
    }


    public void setProjectName(final String projectName) {
        this.projectName = projectName;
    }


    public String getFeatureFile() {
        return featureFile;
    }


    public void setFeatureFile(final String featureFile) {
        this.featureFile = featureFile;
    }


    public String getSubstepsFile() {
        return substepsFile;
    }


    public void setSubstepsFile(final String substepsFile) {
        this.substepsFile = substepsFile;
    }


    public Collection<String> getBeforeAndAfterProcessors() {
        return beforeAndAfterProcessors;
    }


    public void setBeforeAndAfterProcessors(final Collection<String> beforeAndAfterProcessors) {
        this.beforeAndAfterProcessors = beforeAndAfterProcessors;
    }


    private IProject project() {
        return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
    }


    private String createStringFrom(final Collection<String> collection) {
        final StringBuilder sb = new StringBuilder();
        if (collection != null) {
            for (final String stepImpl : collection) {
                sb.append(stepImpl);
                sb.append(";");
            }
        }
        return sb.toString();
    }

}
