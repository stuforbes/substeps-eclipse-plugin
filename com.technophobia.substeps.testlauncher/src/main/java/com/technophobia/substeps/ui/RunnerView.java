package com.technophobia.substeps.ui;

import org.eclipse.swt.widgets.Composite;

import com.technophobia.eclipse.ui.Disposable;
import com.technophobia.substeps.ui.session.SubstepsTestExecutionReporter;

public interface RunnerView extends Disposable {

    void createPartControl(Composite parent);


    SubstepsTestExecutionReporter executionReporter();
}
