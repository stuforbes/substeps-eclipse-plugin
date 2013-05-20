package com.technophobia.substeps.ui.results;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.PaintObjectListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPartSite;

import com.technophobia.eclipse.transformer.Callback1;
import com.technophobia.eclipse.transformer.Locator;
import com.technophobia.eclipse.ui.Disposable;
import com.technophobia.substeps.FeatureRunnerPlugin;
import com.technophobia.substeps.colour.ColourManager;
import com.technophobia.substeps.junit.ui.SubstepsIconProvider;
import com.technophobia.substeps.navigation.JumpToEditorLineCallback;
import com.technophobia.substeps.supplier.Callback2;
import com.technophobia.substeps.supplier.Supplier;
import com.technophobia.substeps.supplier.Transformer;
import com.technophobia.substeps.ui.component.StyledDocumentUpdater;
import com.technophobia.substeps.ui.component.StyledDocumentUpdater.HighlightEvent;
import com.technophobia.substeps.ui.component.SubstepsIcon;
import com.technophobia.substeps.ui.component.TextModelFragment;
import com.technophobia.substeps.ui.event.ClickOnStyledTextLineMouseListener;
import com.technophobia.substeps.ui.event.LineClickHandler;
import com.technophobia.substeps.ui.event.ShowInErrorPaneCallback;
import com.technophobia.substeps.ui.highlight.InstanceAwareDocumentHighlightToStyleRangeTransformer;
import com.technophobia.substeps.ui.model.DocumentHighlight;
import com.technophobia.substeps.ui.model.HierarchicalIconContainer;
import com.technophobia.substeps.ui.model.IconHighlight;
import com.technophobia.substeps.ui.model.StyledDocument;
import com.technophobia.substeps.ui.model.TextHighlight;
import com.technophobia.substeps.ui.paint.PaintRenderedTextListener;

public class StandardTestResultsView implements TestResultsView, Disposable {

    public static final RGB WHITE = new RGB(255, 255, 255);
    public static final RGB GREY = new RGB(128, 128, 128);
    protected static final int IMAGE_HEIGHT = 10;
    protected static final int IMAGE_WIDTH = 10;

    private final StyledDocumentUpdater documentUpdater;
    private final PaintObjectListener paintIconsListener;
    private Transformer<DocumentHighlight, StyleRange> documentHighlightToStyleRangeTransformer;
    private final Callback2<IProject, String> jumpToLineInEditorCallback;

    private StyledText textComponent;
    private IProject currentProject;

    private List<HierarchicalIconContainer> icons;
    private final ColourManager colourManager;
    private final Callback1<String> errorViewCallback;
    private final Locator<TextModelFragment, Integer> textModelFragmentAtOffsetLocator;


    public StandardTestResultsView(final IWorkbenchPartSite site, final SubstepsIconProvider iconProvider,
            final ColourManager colourManager, final Callback1<String> errorViewCallback,
            final Locator<TextModelFragment, Integer> textModelFragmentAtOffsetLocator) {
        this.colourManager = colourManager;
        this.errorViewCallback = errorViewCallback;
        this.textModelFragmentAtOffsetLocator = textModelFragmentAtOffsetLocator;
        this.icons = new ArrayList<HierarchicalIconContainer>();
        this.documentUpdater = updateTextComponentCallback();
        this.jumpToLineInEditorCallback = new JumpToEditorLineCallback(site);
        this.paintIconsListener = new PaintRenderedTextListener(iconProvider, supplyIcons());

        this.currentProject = null;
    }


    @Override
    public void initialise(final Composite parent) {
        this.textComponent = createTextComponent(parent);
        this.documentHighlightToStyleRangeTransformer = initDocumentHighlightToStyleRangeTransformer(colourManager);

        textComponent.setFont(new Font(parent.getDisplay(), parent.getFont().getFontData()[0].name, 10, SWT.NORMAL));
        textComponent.setLineSpacing(5);
        textComponent.setEditable(false);

        textComponent.addMouseListener(new ClickOnStyledTextLineMouseListener(doJumpToEditorCallback()));
        textComponent.addMouseListener(new ClickOnStyledTextLineMouseListener(new ShowInErrorPaneCallback(
                errorViewCallback, textModelFragmentAtOffsetLocator)));
        textComponent.addPaintObjectListener(paintIconsListener);

    }


    @Override
    public void dispose() {
        this.textComponent.removePaintObjectListener(paintIconsListener);
        this.textComponent.dispose();
        this.textComponent = null;

        this.icons.clear();
        this.icons = null;

        this.currentProject = null;
    }


    @Override
    public Control getControl() {
        return textComponent;
    }


    @Override
    public void updateCurrentProject(final IProject project) {
        this.currentProject = project;
    }


    @Override
    public StyledDocumentUpdater documentUpdater() {
        return documentUpdater;
    }


    protected StyledText createTextComponent(final Composite parent) {
        return new StyledText(parent, SWT.NONE);
    }


    protected void resetTextTo(final StyledDocument styledDocument) {
        setTextTo(styledDocument.getText());

        updateStyleRangesTo(styledDocument);
    }


    protected void clearText() {
        setTextTo("");
    }


    protected void setTextTo(final String text) {
        textComponent.setStyleRange(null);
        textComponent.setText(text);
    }


    protected void updateStyleRangesTo(final StyledDocument styledDocument) {
        final int lineCount = textComponent.getLineCount();
        this.icons = new ArrayList<HierarchicalIconContainer>(lineCount);
        prepareTextStyleRanges(textComponent.getLineCount(), styledDocument.getOffsetToParentOffsetMapping());
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


    protected HierarchicalIconContainer createIconContainer(final HierarchicalIconContainer parentContainer,
            final IconHighlight icon) {
        return new HierarchicalIconContainer(true, icon, parentContainer);
    }


    protected Transformer<DocumentHighlight, StyleRange> documentHighlightToStyleRangeTransformer() {
        return documentHighlightToStyleRangeTransformer;
    }


    protected void addHighlight(final DocumentHighlight highlight) {
        final StyleRange styleRange = documentHighlightToStyleRangeTransformer.from(highlight);

        // We never want the style range to be longer than the document,
        // otherwise an IllegalArgumentException is thrown. Instead,
        // make sure the style range is trimmed to the length of the document if
        // required
        styleRange.length = Math.min(styleRange.length, textComponent.getCharCount() - styleRange.start);
        textComponent.setStyleRange(styleRange);
    }


    protected Transformer<DocumentHighlight, StyleRange> initDocumentHighlightToStyleRangeTransformer(
            final ColourManager colourManager) {
        return new InstanceAwareDocumentHighlightToStyleRangeTransformer(colourManager);
    }


    protected void updateIconAt(final int offset, final HighlightEvent highlightEvent) {

        final int line = textComponent.getLineAtOffset(offset);

        // we don't update line 0 - the feature line. As such, the offset/image
        // list starts at line 1, so subtract 1 from this accordingly
        if (line > 0) {
            updateIconAtLine(line - 1, highlightEvent);
        }
    }


    protected void updateIconAtLine(final int line, final HighlightEvent highlightEvent) {
        if (HighlightEvent.TestPassed.equals(highlightEvent)) {
            icons.get(line).mutateIconTo(SubstepsIcon.SubstepPassed);
        } else if (HighlightEvent.TestFailed.equals(highlightEvent)) {
            icons.get(line).mutateIconTo(SubstepsIcon.SubstepFailed);
        } else if (HighlightEvent.NoChange.equals(highlightEvent)) {
            // No-op
        } else {
            FeatureRunnerPlugin.log(IStatus.WARNING, "Unexpected highlight event type");
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


    private LineClickHandler doJumpToEditorCallback() {
        return new LineClickHandler() {
            @Override
            public void onLineClick(final int offset, final String line) {
                jumpToLineInEditorCallback.doCallback(currentProject, line);

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


    private StyledDocumentUpdater updateTextComponentCallback() {
        return new StyledDocumentUpdater() {

            @Override
            public void highlightChanged(final HighlightEvent highlightEvent, final DocumentHighlight highlight) {
                textComponent.getDisplay().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        addHighlight(highlight);

                        updateIconAt(highlight.getOffset(), highlightEvent);
                    }
                });
            }


            @Override
            public void tearDown() {
                textComponent.getDisplay().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        clearText();
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
