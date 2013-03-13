package com.technophobia.substeps.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.Position;
import org.eclipse.swt.graphics.RGB;

import com.technophobia.substeps.FeatureRunnerPlugin;
import com.technophobia.substeps.supplier.Transformer;
import com.technophobia.substeps.ui.component.HierarchicalTextStructure;
import com.technophobia.substeps.ui.component.StyledDocumentUpdater;
import com.technophobia.substeps.ui.component.StyledDocumentUpdater.HighlightEvent;
import com.technophobia.substeps.ui.component.TextModelFragment;
import com.technophobia.substeps.ui.component.TextModelFragment.TextState;
import com.technophobia.substeps.ui.model.DocumentHighlight;
import com.technophobia.substeps.ui.model.StyledDocument;
import com.technophobia.substeps.ui.model.TextHighlight;

public class TextChangedToDocumentUpdater implements TextHighlighter {

    private static final Map<TextState, Transformer<TextModelFragment, DocumentHighlight>> textModelTypeToColourMap = initTextModelTypeToDocumentHighlightMap();

    private final StyledDocumentUpdater updater;


    public TextChangedToDocumentUpdater(final StyledDocumentUpdater updater) {
        this.updater = updater;
    }


    @Override
    public void highlight(final HierarchicalTextStructure t) {
        final TextModelFragment textFragment = (TextModelFragment) t;
        // We don't update the feature line at the very top
        if (textFragment.lineNumber() > 0) {
            updater.highlightChanged(highlightEventFor(textFragment.textState()), toHighlight(textFragment));
        }
    }


    @Override
    public void documentChanged(final String document, final Iterable<HierarchicalTextStructure> textStructures,
            final List<Position> positions) {

        final List<DocumentHighlight> highlights = new ArrayList<DocumentHighlight>();
        final Map<Integer, Integer> offsetToParentOffsetMapping = new HashMap<Integer, Integer>();
        for (final HierarchicalTextStructure textStructure : textStructures) {
            final TextModelFragment textFragment = (TextModelFragment) textStructure;
            highlights.add(toHighlight(textFragment));
            final TextModelFragment parent = textFragment.parentOrNull();
            if (parent != null) {
                offsetToParentOffsetMapping.put(textStructure.offset(), parent.offset());
            }
        }
        updater.documentChanged(new StyledDocument(document, highlights, positions, offsetToParentOffsetMapping));
    }


    @Override
    public void reset() {
        updater.tearDown();

    }


    private HighlightEvent highlightEventFor(final TextState textState) {
        if (TextState.InProgress.equals(textState)) {
            return HighlightEvent.NoChange;
        } else if (TextState.Passed.equals(textState)) {
            return HighlightEvent.TestPassed;
        } else if (TextState.Failed.equals(textState) || TextState.SubNodeFailed.equals(textState)) {
            return HighlightEvent.TestFailed;
        }
        FeatureRunnerPlugin.log(IStatus.WARNING, "Unsure of highlight event for text state " + textState);
        return null;
    }


    private DocumentHighlight toHighlight(final TextModelFragment textFragment) {
        return textModelTypeToColourMap.get(textFragment.textState()).from(textFragment);
    }


    private static Map<TextState, Transformer<TextModelFragment, DocumentHighlight>> initTextModelTypeToDocumentHighlightMap() {
        final Map<TextState, Transformer<TextModelFragment, DocumentHighlight>> results = new HashMap<TextState, Transformer<TextModelFragment, DocumentHighlight>>();

        results.put(TextState.Unprocessed, withColour(128, 128, 128));
        results.put(TextState.InProgress, boldWithColour(0, 0, 0));
        results.put(TextState.Passed, withColour(24, 171, 57));
        results.put(TextState.Failed, withColour(255, 54, 32));
        results.put(TextState.SubNodeFailed, withColour(0, 0, 0));
        return results;
    }


    private static Transformer<TextModelFragment, DocumentHighlight> withColour(final int r, final int g, final int b) {
        return new Transformer<TextModelFragment, DocumentHighlight>() {
            @Override
            public DocumentHighlight from(final TextModelFragment textFragment) {
                // Note the +1 for the offset. The offset is for the line, and
                // position
                // 0 of the line is reserved for the image StyleRange. By not
                // using +1
                // here, you will
                // overwrite the lines image
                return new TextHighlight(textFragment.offset() + 1, textFragment.length(), new RGB(r, g, b));
            }
        };
    }


    private static Transformer<TextModelFragment, DocumentHighlight> boldWithColour(final int r, final int g,
            final int b) {
        return new Transformer<TextModelFragment, DocumentHighlight>() {
            @Override
            public DocumentHighlight from(final TextModelFragment textFragment) {
                // Note the +1 for the offset. The offset is for the line, and
                // position
                // 0 of the line is reserved for the image StyleRange. By not
                // using +1
                // here, you will
                // overwrite the lines image
                return new TextHighlight(textFragment.offset() + 1, textFragment.length(), true, new RGB(r, g, b));
            }
        };
    }
}
