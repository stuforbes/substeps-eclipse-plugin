package com.technophobia.substeps.ui;

import org.eclipse.swt.widgets.Composite;

import com.technophobia.substeps.ui.session.SubstepsTestExecutionReporter;

public interface RunnerView {

    void createPartControl(Composite parent);


    void dispose();


    SubstepsTestExecutionReporter executionReporter();
}
