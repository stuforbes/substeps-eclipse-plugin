package com.technophobia.substeps.ui.component;

import static com.technophobia.substeps.util.LogicOperators.not;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.graphics.RGB;

import com.technophobia.eclipse.transformer.Callback1;
import com.technophobia.substeps.FeatureRunnerPlugin;
import com.technophobia.substeps.supplier.Transformer;
import com.technophobia.substeps.ui.component.StyledDocumentUpdater.HighlightEvent;
import com.technophobia.substeps.ui.component.TextModelFragment.TextState;
import com.technophobia.substeps.ui.model.DocumentHighlight;
import com.technophobia.substeps.ui.model.StyledDocument;
import com.technophobia.substeps.ui.session.SubstepsTestExecutionReporter;

public class StyledDocumentSubstepsTextExecutionReporter implements SubstepsTestExecutionReporter {

    private static final String NULL_PARENT_ID = "-1";

    private static final Map<TextState, Transformer<TextModelFragment, DocumentHighlight>> textModelTypeToColourMap = initTextModelTypeToDocumentHighlightMap();

    private final List<TextModelFragment> textFragments;

    private int currentLength;
    private int currentLine;

    private final StyledDocumentUpdater documentUpdater;


    public StyledDocumentSubstepsTextExecutionReporter(final StyledDocumentUpdater documentUpdater) {
        this.documentUpdater = documentUpdater;
        this.textFragments = new ArrayList<TextModelFragment>();
        this.currentLength = 0;
        this.currentLine = 0;
    }


    @Override
    public void addExecutionNode(final String id, final String parentNodeId, final String text) {
        // We don't add the root 'Features' node
        if (not(NULL_PARENT_ID.equals(parentNodeId))) {
            final TextModelFragment parent = findNodeWithIdOrNull(parentNodeId);

            TextModelFragment fragment;
            if (parent != null) {
                fragment = parent.createChild(id, text, currentLength, currentLine);
            } else {
                fragment = TextModelFragment.createRootFragment(id, text, currentLength, currentLine,
                        textFragmentStateChanged());
            }
            textFragments.add(fragment);
            currentLength += fragment.length() + 1;
            currentLine++;
        }
    }


    @Override
    public void executingNode(final String id) {
        final TextModelFragment textFragment = findNodeWithIdOrNull(id);
        if (textFragment != null) {
            textFragment.markInProgress();
        } else {
            FeatureRunnerPlugin.log(IStatus.WARNING, "Could not mark node with id " + id
                    + " as in progress, as it could not be located");
        }
    }


    @Override
    public void nodeCompleted(final String id) {
        final TextModelFragment textFragment = findNodeWithIdOrNull(id);
        if (textFragment != null) {
            textFragment.markComplete();

            documentUpdater.highlightChanged(HighlightEvent.TestPassed, toHighlight(textFragment));
        } else {
            FeatureRunnerPlugin.log(IStatus.WARNING, "Could not mark node with id " + id
                    + " as complete, as it could not be located");
        }
    }


    @Override
    public void nodeFailed(final String id) {
        final TextModelFragment textFragment = findNodeWithIdOrNull(id);
        if (textFragment != null) {
            textFragment.markFailed();
        } else {
            FeatureRunnerPlugin.log(IStatus.WARNING, "Could not mark node with id " + id
                    + " as complete, as it could not be located");
        }
    }


    @Override
    public void resetExecutionState() {
        this.textFragments.clear();
        this.currentLength = 0;
        this.currentLine = 0;
    }


    @Override
    public void allExecutionNodesAdded() {
        documentUpdater.documentChanged(textFragmentsAsStyledDocument());
    }


    private StyledDocument textFragmentsAsStyledDocument() {
        final StringBuilder sb = new StringBuilder();
        final List<DocumentHighlight> highlights = new ArrayList<DocumentHighlight>();
        for (final TextModelFragment textFragment : textFragments) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(textFragment.indentedText());
            highlights.add(toHighlight(textFragment));
        }
        return new StyledDocument(sb.toString(), highlights);
    }


    private Callback1<TextModelFragment> textFragmentStateChanged() {
        return new Callback1<TextModelFragment>() {

            @Override
            public void callback(final TextModelFragment textFragment) {
                documentUpdater
                        .highlightChanged(highlightEventFor(textFragment.textState()), toHighlight(textFragment));
            }
        };
    }


    protected HighlightEvent highlightEventFor(final TextState textState) {
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


    private TextModelFragment findNodeWithIdOrNull(final String id) {
        // optimisation - the parent of this node is more likely to be nearby to
        // the current node, not the start of the document.
        // Therefore, start at the end and work backwards
        for (int i = textFragments.size() - 1; i >= 0; i--) {
            if (id.equals(textFragments.get(i).id())) {
                return textFragments.get(i);
            }
        }
        return null;
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
                return new DocumentHighlight(textFragment.lineNumber(), textFragment.length(), new RGB(r, g, b));
            }
        };
    }


    private static Transformer<TextModelFragment, DocumentHighlight> boldWithColour(final int r, final int g,
            final int b) {
        return new Transformer<TextModelFragment, DocumentHighlight>() {
            @Override
            public DocumentHighlight from(final TextModelFragment textFragment) {
                return new DocumentHighlight(textFragment.lineNumber(), textFragment.length(), true, new RGB(r, g, b));
            }
        };
    }
}
