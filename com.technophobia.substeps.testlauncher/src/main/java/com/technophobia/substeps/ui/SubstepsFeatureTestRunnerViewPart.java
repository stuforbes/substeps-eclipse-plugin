package com.technophobia.substeps.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import com.technophobia.substeps.FeatureRunnerPlugin;
import com.technophobia.substeps.colour.ColourManager;
import com.technophobia.substeps.supplier.Callback1;
import com.technophobia.substeps.ui.component.SubstepsTextModelExecutionReporter;
import com.technophobia.substeps.ui.run.SubstepsSessionListenerManager;
import com.technophobia.substeps.ui.session.UpdateViewWithSessionListener;

public class SubstepsFeatureTestRunnerViewPart extends ViewPart {

    private SubstepsSessionListenerManager substepsSessionManager;

    private StyledText textComponent;

    private ColourManager colourManager;

    private final RGB[] colours = new RGB[] { //
    new RGB(255, 0, 0), //
            new RGB(0, 255, 0), //
            new RGB(0, 0, 255), //
            new RGB(0, 0, 0), //
            new RGB(128, 128, 128), //
            new RGB(200, 130, 64) //
    };


    @Override
    public void init(final IViewSite site, final IMemento memento) throws PartInitException {
        super.init(site, memento);

        this.colourManager = new ColourManager();

        this.substepsSessionManager = new SubstepsSessionListenerManager(
                FeatureRunnerPlugin.instance().getModel(),
                new UpdateViewWithSessionListener(new SubstepsTextModelExecutionReporter(updateTextComponentCallback())));
    }


    @Override
    public void dispose() {
        this.colourManager.dispose();
        this.colourManager = null;

        super.dispose();
    }


    @Override
    public void createPartControl(final Composite parent) {

        textComponent = new StyledText(parent, SWT.NONE);
        final Font font = new Font(parent.getDisplay(), parent.getFont().getFontData()[0].name, 10, SWT.NORMAL);
        textComponent.setFont(font);
        textComponent.setLineSpacing(5);
        textComponent.setEditable(false);
    }


    @Override
    public void setFocus() {
        // TODO Auto-generated method stub

    }


    private Callback1<String> updateTextComponentCallback() {
        return new Callback1<String>() {
            @Override
            public void doCallback(final String t) {
                getSite().getShell().getDisplay().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        textComponent.setStyleRange(null);
                        textComponent.setText(t);
                    }
                });
            }
        };
    }
}
