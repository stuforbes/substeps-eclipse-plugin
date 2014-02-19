package com.technophobia.substeps.view.dependencies;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

import com.technophobia.substeps.execution.node.IExecutionNode;

public class StepDependenciesLabelProvider implements ILabelProvider {

    @Override
    public Image getImage(final Object element) {
        return null;
    }


    @Override
    public String getText(final Object element) {
        return ((IExecutionNode) element).getDescription();
    }


    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }


    @Override
    public boolean isLabelProperty(final Object element, final String property) {
        return false;
    }


    @Override
    public void addListener(final ILabelProviderListener listener) {
        // No-op
    }


    @Override
    public void removeListener(final ILabelProviderListener listener) {
        // No-op
    }
}
