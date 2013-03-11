package com.technophobia.substeps.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.PaintObjectListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;

import com.technophobia.eclipse.transformer.Callback1;
import com.technophobia.substeps.FeatureRunnerPlugin;
import com.technophobia.substeps.colour.ColourManager;
import com.technophobia.substeps.junit.ui.SubstepsIconProvider;
import com.technophobia.substeps.supplier.Supplier;
import com.technophobia.substeps.supplier.Transformer;
import com.technophobia.substeps.ui.component.ListDelegateHierarchicalTextCollection;
import com.technophobia.substeps.ui.component.StyledDocumentSubstepsTextExecutionReporter;
import com.technophobia.substeps.ui.component.StyledDocumentUpdater;
import com.technophobia.substeps.ui.component.StyledDocumentUpdater.HighlightEvent;
import com.technophobia.substeps.ui.component.SubstepsIcon;
import com.technophobia.substeps.ui.component.TextModelFragmentFactory;
import com.technophobia.substeps.ui.model.DocumentHighlight;
import com.technophobia.substeps.ui.model.IconHighlight;
import com.technophobia.substeps.ui.model.StyledDocument;
import com.technophobia.substeps.ui.model.TextHighlight;
import com.technophobia.substeps.ui.session.SubstepsTestExecutionReporter;

public class StyledTextRunnerView implements RunnerView {

    protected static final RGB WHITE = new RGB(255, 255, 255);
    protected static final RGB GREY = new RGB(128, 128, 128);
    protected static final int IMAGE_HEIGHT = 10;
    protected static final int IMAGE_WIDTH = 10;

    private StyledText textComponent;
    private PaintObjectListener paintIconsListener;

    private List<HierarchicalIconContainer> icons;

    private final StyledDocumentUpdater styledDocumentUpdater;
    private final SubstepsIconProvider iconProvider;
    private final Transformer<DocumentHighlight, StyleRange> documentHighlightToStyleRangeTransformer;


    public StyledTextRunnerView(final ColourManager colourManager, final SubstepsIconProvider iconProvider) {
        this.iconProvider = iconProvider;
        this.documentHighlightToStyleRangeTransformer = new InstanceAwareDocumentHighlightToStyleRangeTransformer(
                colourManager);

        this.icons = new ArrayList<HierarchicalIconContainer>();

        this.styledDocumentUpdater = updateTextComponentCallback();
    }


    @Override
    public void createPartControl(final Composite parent) {

        textComponent = createTextComponent(parent);
        this.paintIconsListener = new PaintRenderedTextListener(iconProvider, supplyIcons());

        // textComponent.setAlwaysShowScrollBars(true);
        final Font font = new Font(parent.getDisplay(), parent.getFont().getFontData()[0].name, 10, SWT.NORMAL);
        textComponent.setFont(font);
        textComponent.setLineSpacing(5);
        textComponent.setEditable(false);

        textComponent.addPaintObjectListener(paintIconsListener);

    }


    @Override
    public void dispose() {
        textComponent.removePaintObjectListener(paintIconsListener);
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
        return new StyledText(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
    }


    protected void resetTextTo(final StyledDocument styledDocument) {
        setTextTo(styledDocument.getText());

        updateStyleRangesTo(styledDocument);
    }


    protected void updateStyleRangesTo(final StyledDocument styledDocument) {
        textComponent.setStyleRange(null);
        final int lineCount = textComponent.getLineCount();
        icons = new ArrayList<HierarchicalIconContainer>(lineCount);
        prepareTextStyleRanges(textComponent.getLineCount(), styledDocument.getOffsetToParentOffsetMapping());
    }


    protected void setTextTo(final String text) {
        textComponent.setText(text);
    }


    private void prepareTextStyleRanges(final int lineCount, final Map<Integer, Integer> offsetToParentOffsetMap) {
        // use positions to determine parent structure - if a positions
        // offset+length is greater than the next pos, then the former is a
        // parent

        final Map<Integer, HierarchicalIconContainer> lineNumberToIconMapping = new HashMap<Integer, HierarchicalIconContainer>();

        for (int i = 1; i < lineCount; i++) {
            final int offset = textComponent.getOffsetAtLine(i);

            final Integer parentOffset = offsetToParentOffsetMap.get(Integer.valueOf(offset));
            final HierarchicalIconContainer parentContainer = parentOffset != null ? lineNumberToIconMapping
                    .get(parentOffset) : null;

            textComponent.replaceTextRange(offset, 1, "\uFFFC");

            createUnprocessedTextHighlight(i, offset);
            createUnprocessedIconHighlight(i, offset, parentContainer, lineNumberToIconMapping);
        }
    }


    private void createUnprocessedTextHighlight(final int line, final int offset) {
        final int length = textComponent.getLine(line).length() - 1;
        // Note the +1 for the offset. The offset is for the line, and position
        // 0 of the line is reserved for the image StyleRange. By not using +1
        // here, you will
        // overwrite the lines image
        addHighlight(new TextHighlight(offset + 1, length, GREY));
    }


    private void createUnprocessedIconHighlight(final int line, final int offset,
            final HierarchicalIconContainer parentContainer,
            final Map<Integer, HierarchicalIconContainer> lineNumberToTextMapping) {
        final IconHighlight icon = new IconHighlight(offset, 1, SubstepsIcon.SubstepNoResult, IMAGE_WIDTH, IMAGE_HEIGHT);
        addHighlight(icon);

        final HierarchicalIconContainer iconContainer = createIconContainer(parentContainer, icon);
        icons.add(iconContainer);
        lineNumberToTextMapping.put(Integer.valueOf(offset), iconContainer);
    }


    protected HierarchicalIconContainer createIconContainer(final HierarchicalIconContainer parentContainer,
            final IconHighlight icon) {
        return new HierarchicalIconContainer(true, icon, parentContainer);
    }


    protected Transformer<DocumentHighlight, StyleRange> documentHighlightToStyleRangeTransformer() {
        return documentHighlightToStyleRangeTransformer;
    }


    protected void addHighlight(final DocumentHighlight highlight) {
        textComponent.setStyleRange(documentHighlightToStyleRangeTransformer.from(highlight));
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


    protected void doIconOperation(final int offset, final int length,
            final Callback1<HierarchicalIconContainer> callback) {
        final int end = offset + length;
        for (final HierarchicalIconContainer iconContainer : icons) {
            final int iconOffset = iconContainer.getOffset();
            if (iconOffset >= offset && iconOffset < end) {
                callback.callback(iconContainer);
            }
        }
    }


    protected void doIconOperation(final int offset, final Callback1<HierarchicalIconContainer> callback) {
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

                        updateIconAt(textComponent.getLineAtOffset(highlight.getOffset()), highlightEvent);
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


    private Supplier<List<HierarchicalIconContainer>> supplyIcons() {
        return new Supplier<List<HierarchicalIconContainer>>() {

            @Override
            public List<HierarchicalIconContainer> get() {
                return icons;
            }
        };
    }
}
