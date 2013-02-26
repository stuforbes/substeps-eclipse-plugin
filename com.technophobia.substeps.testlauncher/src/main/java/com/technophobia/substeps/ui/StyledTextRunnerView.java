package com.technophobia.substeps.ui;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;

import com.technophobia.substeps.colour.ColourManager;
import com.technophobia.substeps.supplier.Callback1;
import com.technophobia.substeps.ui.component.StyledDocumentSubstepsTextExecutionReporter;
import com.technophobia.substeps.ui.model.DocumentHighlight;
import com.technophobia.substeps.ui.model.StyledDocument;
import com.technophobia.substeps.ui.session.SubstepsTestExecutionReporter;

public class StyledTextRunnerView implements RunnerView {

    private static final RGB WHITE = new RGB(255, 255, 255);

    private StyledText textComponent;
    private final ColourManager colourManager;


    public StyledTextRunnerView(final ColourManager colourManager) {
        this.colourManager = colourManager;
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
    public void dispose() {
        textComponent.dispose();
        textComponent = null;
    }


    @Override
    public SubstepsTestExecutionReporter executionReporter() {
        return new StyledDocumentSubstepsTextExecutionReporter(updateTextComponentCallback());
    }


    protected void resetTextTo(final StyledDocument document) {
        textComponent.setStyleRange(null);
        textComponent.setText(document.getText());
    }


    protected void addHighlights(final List<DocumentHighlight> highlights) {
        for (final DocumentHighlight highlight : highlights) {
            textComponent.setStyleRange(new StyleRange(highlight.getOffset(), highlight.getLength(), colourManager
                    .getColor(highlight.getColour()), colourManager.getColor(WHITE)));
        }
    }


    private Callback1<StyledDocument> updateTextComponentCallback() {
        return new Callback1<StyledDocument>() {
            @Override
            public void doCallback(final StyledDocument document) {
                textComponent.getDisplay().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        resetTextTo(document);

                        addHighlights(document.getHighlights());
                    }
                });
            }
        };
    }
}
