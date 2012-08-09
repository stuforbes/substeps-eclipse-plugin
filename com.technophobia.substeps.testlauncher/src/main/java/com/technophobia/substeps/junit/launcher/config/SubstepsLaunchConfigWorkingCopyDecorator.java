package com.technophobia.substeps.junit.launcher.config;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

import com.technophobia.eclipse.launcher.config.SubstepsLaunchConfigurationConstants;
import com.technophobia.eclipse.launcher.exception.ExceptionReporter;
import com.technophobia.eclipse.transformer.Decorator;
import com.technophobia.eclipse.transformer.Transformer;
import com.technophobia.substeps.junit.launcher.SubstepsFeatureLaunchShortcut;

public class SubstepsLaunchConfigWorkingCopyDecorator implements Decorator<ILaunchConfigurationWorkingCopy, IResource> {

    public static final String FEATURE_TEST = "com.technophobia.substeps.runner.runtime.DefinableFeatureTest";

    private final Transformer<IProject, IJavaProject> javaProjectTransformer;
    private final ExceptionReporter exceptionReporter;

    private final Transformer<IProject, String> substepsFolderLocator;


    public SubstepsLaunchConfigWorkingCopyDecorator(final Transformer<IProject, IJavaProject> javaProjectTransformer,
            final ExceptionReporter exceptionReporter, final Transformer<IProject, String> substepsFolderLocator) {
        this.javaProjectTransformer = javaProjectTransformer;
        this.exceptionReporter = exceptionReporter;
        this.substepsFolderLocator = substepsFolderLocator;
    }


    @Override
    public void decorate(final ILaunchConfigurationWorkingCopy workingCopy, final IResource resource) {

        final IProject project = resource.getProject();
        final String filePath = resource.getFullPath().removeFirstSegments(1).toOSString();

        workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, FEATURE_TEST);
        workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, project.getName());
        workingCopy.setAttribute(SubstepsLaunchConfigurationConstants.ATTR_KEEPRUNNING, false);
        workingCopy.setAttribute(SubstepsLaunchConfigurationConstants.ATTR_TEST_CONTAINER, "");
        workingCopy.setAttribute(SubstepsLaunchConfigurationConstants.ATTR_TEST_RUNNER_KIND,
                SubstepsLaunchConfigurationConstants.JUNIT4_TEST_KIND_ID);

        workingCopy.setAttribute(SubstepsFeatureLaunchShortcut.ATTR_FEATURE_FILE, filePath);
        workingCopy.setAttribute(SubstepsLaunchConfigurationConstants.ATTR_FEATURE_PROJECT, project.getName());

        final String substepsFolder = substepsFolderLocator.to(project);
        workingCopy.setAttribute(SubstepsLaunchConfigurationConstants.ATTR_SUBSTEPS_FILE,
                substepsFolder != null ? substepsFolder : "");
        // workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS,
        // vmArgs(filePath, project));
    }


    private String outputFolderFor(final IProject project) {
        try {
            final IPath outputLocation = javaProjectTransformer.to(project).getOutputLocation();
            // remove project name
            return outputLocation.removeFirstSegments(1).toOSString();
        } catch (final JavaModelException ex) {
            exceptionReporter.report(ex);
            return null;
        }
    }
}
