package com.technophobia.substeps.ui;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import com.technophobia.substeps.FeatureRunnerPlugin;
import com.technophobia.substeps.colour.ColourManager;
import com.technophobia.substeps.junit.ui.SubstepsIconProvider;
import com.technophobia.substeps.ui.run.SubstepsSessionListenerManager;
import com.technophobia.substeps.ui.session.UpdateViewWithSessionListener;

public class SubstepsFeatureTestRunnerViewPart extends ViewPart {

    public static final String NAME = "com.technophobia.substeps.runner.SubstepsResultView";

    private SubstepsSessionListenerManager substepsSessionManager;

    private RunnerView runnerView;

    private ColourManager colourManager;

    private SubstepsIconProvider iconProvider;


    @Override
    public void init(final IViewSite site, final IMemento memento) throws PartInitException {
        super.init(site, memento);

        this.colourManager = new ColourManager();
        this.iconProvider = new SubstepsIconProvider(new ImageDescriptorLoader());
        this.runnerView = new CodeFoldingStyleTextRunnerView(colourManager, iconProvider, getSite());
        // this.runnerView = new StyledTextRunnerView(colourManager,
        // iconProvider, getSite());

        this.substepsSessionManager = new SubstepsSessionListenerManager(new UpdateViewWithSessionListener(
                runnerView.executionReporter()));

        substepsSessionManager.registerListenersOn(FeatureRunnerPlugin.instance().getModel());
    }


    @Override
    public void dispose() {
        this.colourManager.dispose();
        this.colourManager = null;

        substepsSessionManager.unRegisterListenersOn(FeatureRunnerPlugin.instance().getModel());
        substepsSessionManager = null;

        iconProvider.dispose();
        iconProvider = null;

        super.dispose();
    }


    @Override
    public void createPartControl(final Composite parent) {
        runnerView.createPartControl(parent);
    }


    @Override
    public void setFocus() {
        // TODO Auto-generated method stub

    }

}
