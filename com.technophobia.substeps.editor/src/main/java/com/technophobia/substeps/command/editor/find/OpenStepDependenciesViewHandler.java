package com.technophobia.substeps.command.editor.find;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

import com.technophobia.substeps.FeatureEditorPlugin;
import com.technophobia.substeps.command.AbstractSubstepsEditorHandler;
import com.technophobia.substeps.view.dependencies.ShowDependenciesViewPart;

public class OpenStepDependenciesViewHandler extends AbstractSubstepsEditorHandler {

    @Override
    protected void doWithLine(final String line, final IProject project, final IWorkbenchPage page) {
        showViewWithProjectStep(line, project, page);
    }


    private void showViewWithProjectStep(final String line, final IProject project, final IWorkbenchPage page) {
        final Display display = page.getWorkbenchWindow().getShell().getDisplay();
        display.asyncExec(new Runnable() {
            @Override
            public void run() {
                final ShowDependenciesViewPart view = showTestRunnerViewPartInActivePage(line, project, page);

                view.showDependenciesForLine(line, project);
            }
        });
    }


    private ShowDependenciesViewPart showTestRunnerViewPartInActivePage(final String line, final IProject project,
            final IWorkbenchPage page) {
        try {
            final ShowDependenciesViewPart view = (ShowDependenciesViewPart) page
                    .findView(ShowDependenciesViewPart.NAME);
            if (view == null) {
                // create and show the result view if it isn't created yet.
                return (ShowDependenciesViewPart) page.showView(ShowDependenciesViewPart.NAME, null,
                        IWorkbenchPage.VIEW_VISIBLE);
            }
            return view;
        } catch (final PartInitException pie) {
            FeatureEditorPlugin.instance().warn("Could not show dependencies view", pie);
            return null;
        }
    }
}
