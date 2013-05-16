package com.technophobia.substeps.ui.results;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.technophobia.substeps.ui.component.StyledDocumentUpdater;

public interface TestResultsView {

    public abstract void initialise(Composite parent);


    public abstract Control getControl();


    public abstract void updateCurrentProject(IProject project);


    public abstract StyledDocumentUpdater documentUpdater();

}