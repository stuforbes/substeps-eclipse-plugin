package com.technophobia.eclipse.transformer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

public class ProjectByNameLocator implements Locator<IProject, String> {

    @Override
    public IProject one(final String projectName) {
        return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
    }

}
