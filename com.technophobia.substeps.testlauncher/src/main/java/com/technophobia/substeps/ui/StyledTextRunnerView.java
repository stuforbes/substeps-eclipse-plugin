package com.technophobia.substeps.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.PaintObjectEvent;
import org.eclipse.swt.custom.PaintObjectListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;

import com.technophobia.eclipse.transformer.Callback1;
import com.technophobia.substeps.FeatureRunnerPlugin;
import com.technophobia.substeps.colour.ColourManager;
import com.technophobia.substeps.junit.ui.SubstepsIconProvider;
import com.technophobia.substeps.supplier.Transformer;
import com.technophobia.substeps.ui.component.ListDelegateHierarchicalTextCollection;
import com.technophobia.substeps.ui.component.StyledDocumentSubstepsTextExecutionReporter;
import com.technophobia.substeps.ui.component.StyledDocumentUpdater;
import com.technophobia.substeps.ui.component.StyledDocumentUpdater.HighlightEvent;
import com.technophobia.substeps.ui.component.SubstepsIcon;
import com.technophobia.substeps.ui.component.TextModelFragmentFactory;
import com.technophobia.substeps.ui.model.DocumentHighlight;
import com.technophobia.substeps.ui.model.StyledDocument;
import com.technophobia.substeps.ui.session.SubstepsTestExecutionReporter;

public class StyledTextRunnerView implements RunnerView {

    private static final RGB WHITE = new RGB(255, 255, 255);
    private static final RGB GREY = new RGB(128, 128, 128);
    private static final int IMAGE_HEIGHT = 10;
    private static final int IMAGE_WIDTH = 10;

    private final Transformer<Integer, Point> offsetToPointTransformer;

    private StyledText textComponent;
    private final ColourManager colourManager;

    private final SubstepsIconProvider iconProvider;

    private List<RenderedText> icons;

    private final StyledDocumentUpdater styledDocumentUpdater;


    public StyledTextRunnerView(final ColourManager colourManager, final SubstepsIconProvider iconProvider) {
        this.colourManager = colourManager;
        this.iconProvider = iconProvider;

        this.icons = new ArrayList<RenderedText>();

        this.styledDocumentUpdater = updateTextComponentCallback();
        this.offsetToPointTransformer = initOffsetToPointTransformer();
    }


    @Override
    public void createPartControl(final Composite parent) {

        textComponent = createTextComponent(parent);

        // textComponent.setAlwaysShowScrollBars(true);
        final Font font = new Font(parent.getDisplay(), parent.getFont().getFontData()[0].name, 10, SWT.NORMAL);
        textComponent.setFont(font);
        textComponent.setLineSpacing(5);
        textComponent.setEditable(false);

        textComponent.addPaintListener(new PaintListener() {

            @Override
            public void paintControl(final PaintEvent event) {
                final GC gc = event.gc;
                // final StyleRange style = event.style;
                // final int start = style.start;
                for (final RenderedText icon : icons) {
                    if (icon.isRendered()) {
                        final Image image = iconProvider.imageFor(icon.getIcon());

                        final Point locationAtOffset = icon.getLocation();
                        gc.drawImage(image, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, locationAtOffset.x,
                                locationAtOffset.y + 2, IMAGE_WIDTH, IMAGE_HEIGHT);
                    }
                }
            }
        });

        textComponent.addPaintObjectListener(new PaintObjectListener() {

            @Override
            public void paintObject(final PaintObjectEvent event) {

            }
        });
    }


    @Override
    public void dispose() {
        textComponent.dispose();
        textComponent = null;

        icons.clear();
    }


    @Override
    public SubstepsTestExecutionReporter executionReporter() {
        final ListDelegateHierarchicalTextCollection textCollection = new ListDelegateHierarchicalTextCollection();
        final TextChangedToDocumentUpdater stateChangeHighlighter = new TextChangedToDocumentUpdater(
                styledDocumentUpdater);
        final TextModelFragmentFactory textModelFragmentFactory = new TextModelFragmentFactory(textCollection,
                stateChangeHighlighter);
        return new StyledDocumentSubstepsTextExecutionReporter(textCollection, textModelFragmentFactory,
                stateChangeHighlighter);
    }


    protected StyledText createTextComponent(final Composite parent) {
        return new StyledText(parent, SWT.BORDER);
    }


    protected void resetTextTo(final StyledDocument styledDocument) {
        textComponent.setStyleRange(null);
        setTextTo(styledDocument.getText());

        final int lineCount = textComponent.getLineCount();
        icons = new ArrayList<RenderedText>(lineCount);
        prepareTextStyleRanges(textComponent.getLineCount(), styledDocument.getOffsetToParentOffsetMapping());

        textComponent.redraw();
    }


    protected void setTextTo(final String text) {
        textComponent.setText(text);
    }


    private void prepareTextStyleRanges(final int lineCount, final Map<Integer, Integer> offsetToParentOffsetMap) {
        // use positions to determine parent structure - if a positions
        // offset+length is greater than the next pos, then the former is a
        // parent

        final Map<Integer, RenderedText> lineNumberToTextMapping = new HashMap<Integer, RenderedText>();

        for (int i = 1; i < lineCount; i++) {
            final int offset = textComponent.getOffsetAtLine(i);

            final Integer parentOffset = offsetToParentOffsetMap.get(Integer.valueOf(offset));

            final RenderedText renderedText = createIconStyleRange(offset,
                    parentOffset != null ? lineNumberToTextMapping.get(parentOffset) : null);
            icons.add(renderedText);
            lineNumberToTextMapping.put(Integer.valueOf(offset), renderedText);

            createUnprocessedTextStyleRange(i, offset);
        }
    }


    private void createUnprocessedTextStyleRange(final int line, final int offset) {
        final int length = textComponent.getLine(line).length() - 1;
        addHighlight(new DocumentHighlight(line, length, GREY));
    }


    protected RenderedText createIconStyleRange(final int offset, final RenderedText parent) {
        final StyleRange style = new StyleRange();
        // textComponent.replaceTextRange(offset, 1, "\uFFFC");
        style.start = offset;
        style.length = 1;
        style.metrics = new GlyphMetrics(IMAGE_HEIGHT, 0, IMAGE_WIDTH);
        textComponent.setStyleRange(style);
        return new RenderedText(true, SubstepsIcon.SubstepNoResult, offset, parent, offsetToPointTransformer);
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
                icons.get(normalizedLine).mutateIconTo(SubstepsIcon.SubstepPassed);
            } else if (HighlightEvent.TestFailed.equals(highlightEvent)) {
                icons.get(normalizedLine).mutateIconTo(SubstepsIcon.SubstepFailed);
            } else if (HighlightEvent.NoChange.equals(highlightEvent)) {
                // No-op
            } else {
                FeatureRunnerPlugin.log(IStatus.WARNING, "Unexpected highlight event type");
            }
        }
    }


    protected void doIconOperation(final int offset, final int length, final Callback1<RenderedText> callback) {
        final int end = offset + length;
        for (final RenderedText icon : icons) {
            if (icon.getOffset() >= offset && icon.getOffset() < end) {
                callback.callback(icon);
            }
        }
    }


    protected void doIconOperation(final int offset, final Callback1<RenderedText> callback) {
        doIconOperation(offset, Integer.MAX_VALUE - offset, callback);
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


    private Transformer<Integer, Point> initOffsetToPointTransformer() {
        return new Transformer<Integer, Point>() {
            @Override
            public Point from(final Integer offset) {
                return textComponent.getLocationAtOffset(offset.intValue());
            }
        };
    }
}
