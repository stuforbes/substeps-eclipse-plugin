package com.technophobia.substeps.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.PaintObjectEvent;
import org.eclipse.swt.custom.PaintObjectListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;

import com.technophobia.substeps.FeatureRunnerPlugin;
import com.technophobia.substeps.colour.ColourManager;
import com.technophobia.substeps.junit.ui.SubstepsIconProvider;
import com.technophobia.substeps.ui.component.StyledDocumentSubstepsTextExecutionReporter;
import com.technophobia.substeps.ui.component.StyledDocumentUpdater;
import com.technophobia.substeps.ui.component.StyledDocumentUpdater.HighlightEvent;
import com.technophobia.substeps.ui.component.SubstepsIcon;
import com.technophobia.substeps.ui.model.DocumentHighlight;
import com.technophobia.substeps.ui.model.StyledDocument;
import com.technophobia.substeps.ui.session.SubstepsTestExecutionReporter;

public class StyledTextRunnerView implements RunnerView {

    private static final RGB WHITE = new RGB(255, 255, 255);
    private static final RGB GREY = new RGB(128, 128, 128);
    private static final int IMAGE_HEIGHT = 10;
    private static final int IMAGE_WIDTH = 10;

    private StyledText textComponent;
    private final ColourManager colourManager;

    private final SubstepsIconProvider iconProvider;

    private List<Integer> offsets;
    private List<SubstepsIcon> images;


    public StyledTextRunnerView(final ColourManager colourManager, final SubstepsIconProvider iconProvider) {
        this.colourManager = colourManager;
        this.iconProvider = iconProvider;

        this.offsets = new ArrayList<Integer>();
        this.images = new ArrayList<SubstepsIcon>();
    }


    @Override
    public void createPartControl(final Composite parent) {
        textComponent = new StyledText(parent, SWT.BORDER | SWT.V_SCROLL);
        // textComponent.setAlwaysShowScrollBars(true);
        final Font font = new Font(parent.getDisplay(), parent.getFont().getFontData()[0].name, 10, SWT.NORMAL);
        textComponent.setFont(font);
        textComponent.setLineSpacing(5);
        textComponent.setEditable(false);

        textComponent.addPaintObjectListener(new PaintObjectListener() {

            @Override
            public void paintObject(final PaintObjectEvent event) {
                final GC gc = event.gc;
                final StyleRange style = event.style;
                final int start = style.start;
                for (int i = 0; i < offsets.size(); i++) {
                    final int offset = offsets.get(i);
                    if (start == offset) {
                        final Image image = iconProvider.imageFor(images.get(i));
                        final int x = event.x;
                        final int y = event.y + event.ascent - style.metrics.ascent;
                        gc.drawImage(image, x, y);
                    }
                }
            }
        });
    }


    @Override
    public void dispose() {
        textComponent.dispose();
        textComponent = null;

        images.clear();
        offsets.clear();
    }


    @Override
    public SubstepsTestExecutionReporter executionReporter() {
        return new StyledDocumentSubstepsTextExecutionReporter(updateTextComponentCallback());
    }


    protected void resetTextTo(final StyledDocument document) {
        textComponent.setStyleRange(null);
        textComponent.setText(document.getText());

        final int lineCount = textComponent.getLineCount();
        images = new ArrayList<SubstepsIcon>(lineCount);
        offsets = new ArrayList<Integer>(lineCount);

        prepareTextStyleRanges(lineCount);
    }


    private void prepareTextStyleRanges(final int lineCount) {
        for (int i = 1; i < lineCount; i++) {
            final int offset = textComponent.getOffsetAtLine(i);
            createUnprocessedTextStyleRange(i, offset);
            createIconStyleRange(offset);
        }
    }


    private void createUnprocessedTextStyleRange(final int line, final int offset) {
        final int length = textComponent.getLine(line).length() - 1;
        textComponent.setStyleRange(new StyleRange(offset + 1, length, colourManager.getColor(GREY), colourManager
                .getColor(WHITE), SWT.NONE));
    }


    private void createIconStyleRange(final int offset) {
        final StyleRange style = new StyleRange();
        textComponent.replaceTextRange(offset, 0, "\uFFFC");
        style.start = offset;
        style.length = 1;
        style.metrics = new GlyphMetrics(IMAGE_HEIGHT, 0, IMAGE_WIDTH);
        textComponent.setStyleRange(style);
        offsets.add(offset);
        images.add(SubstepsIcon.SubstepNoResult);
    }


    protected void addHighlight(final DocumentHighlight highlight) {
        // StyleRange offset is line number offset + 1 - this is because the
        // result icon is at position 0
        textComponent.setStyleRange(new StyleRange(textComponent.getOffsetAtLine(highlight.getLine()) + 1, highlight
                .getLength(), colourManager.getColor(highlight.getColour()), colourManager.getColor(WHITE), highlight
                .isBold() ? SWT.BOLD : SWT.NONE));
    }


    protected void updateIconAt(final int line, final HighlightEvent highlightEvent) {

        // we don't update line 0 - the feature line. As such, the offset/image
        // list starts at line 1, so subtract 1 from this accordingly
        if (line > 0) {
            final int normalizedLine = line - 1;

            if (HighlightEvent.TestPassed.equals(highlightEvent)) {
                images.remove(normalizedLine);
                images.add(normalizedLine, SubstepsIcon.SubstepPassed);
            } else if (HighlightEvent.TestFailed.equals(highlightEvent)) {
                images.remove(normalizedLine);
                images.add(normalizedLine, SubstepsIcon.SubstepFailed);
            } else if (HighlightEvent.NoChange.equals(highlightEvent)) {
                // No change
            } else {
                FeatureRunnerPlugin.log(IStatus.WARNING, "Unexpected highlight event type");
            }
        }
    }


    private StyledDocumentUpdater updateTextComponentCallback() {
        return new StyledDocumentUpdater() {

            @Override
            public void highlightChanged(final HighlightEvent highlightEvent, final DocumentHighlight highlight) {
                textComponent.getDisplay().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        addHighlight(highlight);

                        updateIconAt(highlight.getLine(), highlightEvent);
                    }
                });
            }


            @Override
            public void documentChanged(final StyledDocument document) {
                textComponent.getDisplay().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        resetTextTo(document);
                    }
                });
            }
        };
    }
}
