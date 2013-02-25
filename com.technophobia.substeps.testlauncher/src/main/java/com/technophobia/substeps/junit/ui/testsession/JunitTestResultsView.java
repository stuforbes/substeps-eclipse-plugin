/*******************************************************************************
 * Copyright Technophobia Ltd 2012
 * 
 * This file is part of the Substeps Eclipse Plugin.
 * 
 * The Substeps Eclipse Plugin is free software: you can redistribute it and/or modify
 * it under the terms of the Eclipse Public License v1.0.
 * 
 * The Substeps Eclipse Plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Eclipse Public License for more details.
 * 
 * You should have received a copy of the Eclipse Public License
 * along with the Substeps Eclipse Plugin.  If not, see <http://www.eclipse.org/legal/epl-v10.html>.
 ******************************************************************************/
package com.technophobia.substeps.junit.ui.testsession;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import com.technophobia.substeps.FeatureRunnerPlugin;
import com.technophobia.substeps.junit.ui.SubstepsFeatureTestRunnerViewPartOld;

public class JunitTestResultsView implements TestResultsView {

    private final IWorkbenchWindow window;


    public JunitTestResultsView(final IWorkbenchWindow window) {
        this.window = window;
    }


    @Override
    public void showTestResultsView() {
        final IWorkbenchPage page = window.getActivePage();
        SubstepsFeatureTestRunnerViewPartOld testRunner = null;

        if (page != null) {
            try { // show the result view
                testRunner = (SubstepsFeatureTestRunnerViewPartOld) page.findView(SubstepsFeatureTestRunnerViewPartOld.NAME);
                if (testRunner == null) {
                    final IWorkbenchPart activePart = page.getActivePart();
                    testRunner = (SubstepsFeatureTestRunnerViewPartOld) page.showView(
                            SubstepsFeatureTestRunnerViewPartOld.NAME, null, IWorkbenchPage.VIEW_VISIBLE);
                    // restore focus
                    page.activate(activePart);
                } else {
                    page.bringToTop(testRunner);
                }
            } catch (final PartInitException pie) {
                FeatureRunnerPlugin.log(IStatus.ERROR, pie.getMessage());
            }
        }
    }

}
