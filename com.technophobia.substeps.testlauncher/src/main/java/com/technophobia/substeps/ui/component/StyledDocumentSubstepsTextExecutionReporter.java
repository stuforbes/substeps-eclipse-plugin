package com.technophobia.substeps.ui.component;

import static com.technophobia.substeps.util.LogicOperators.not;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.RGB;

import com.technophobia.substeps.supplier.Callback1;
import com.technophobia.substeps.ui.model.DocumentHighlight;
import com.technophobia.substeps.ui.model.StyledDocument;
import com.technophobia.substeps.ui.session.SubstepsTestExecutionReporter;

public class StyledDocumentSubstepsTextExecutionReporter implements SubstepsTestExecutionReporter {

    private static final String NULL_PARENT_ID = "-1";

    private final List<TextModelFragment> textFragments;

    private int currentLength;

    private final Callback1<StyledDocument> textModelCallback;


    public StyledDocumentSubstepsTextExecutionReporter(final Callback1<StyledDocument> textModelCallback) {
        this.textModelCallback = textModelCallback;
        this.textFragments = new ArrayList<TextModelFragment>();
        this.currentLength = 0;
    }


    @Override
    public void addExecutionNode(final String id, final String parentNodeId, final String text) {
        // We don't add the root 'Features' node
        if (not(NULL_PARENT_ID.equals(parentNodeId))) {
            final int depth = depthFor(id, parentNodeId);
            final int textLength = text.length() + depth;
            textFragments.add(new TextModelFragment(id, text, depth, currentLength, textLength));

            currentLength += textLength + 1;
        }
    }


    @Override
    public void resetExecutionState() {
        this.textFragments.clear();
        this.currentLength = 0;
    }


    @Override
    public void allExecutionNodesAdded() {
        textModelCallback.doCallback(textFragmentsAsStyledDocument());
    }


    private StyledDocument textFragmentsAsStyledDocument() {
        final StringBuilder sb = new StringBuilder();
        final List<DocumentHighlight> highlights = new ArrayList<DocumentHighlight>();
        for (final TextModelFragment textFragment : textFragments) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(textFragment.indentedText());
            highlights.add(new DocumentHighlight(textFragment.startPosition(), textFragment.length(), new RGB(128, 128,
                    128)));
        }
        return new StyledDocument(sb.toString(), highlights);
    }


    private int depthFor(final String id, final String parentNodeId) {
        if (parentNodeId == null) {
            return 0;
        } else if (isChildOfLast(parentNodeId)) {
            return currentDepth() + 1;
        } else {
            final TextModelFragment fragment = findNodeWithIdOrNull(parentNodeId);
            if (fragment != null) {
                return fragment.depth() + 1;
            }

            // couldn't find fragment (should never happen)
            return 0;
        }
    }


    private boolean isChildOfLast(final String parentNodeId) {
        if (not(textFragments.isEmpty())) {
            final TextModelFragment lastElement = textFragments.get(textFragments.size() - 1);
            return parentNodeId != null && parentNodeId.equals(lastElement.id());
        }

        // no text fragments, therefore this item isn't a child
        return false;
    }


    private int currentDepth() {
        if (not(textFragments.isEmpty())) {
            final TextModelFragment lastElement = textFragments.get(textFragments.size() - 1);
            return lastElement.depth();
        }

        // no items, so current depth must be 0
        return 0;
    }


    private TextModelFragment findNodeWithIdOrNull(final String parentNodeId) {
        // optimisation - the parent of this node is more likely to be nearby to
        // the current node, not the start of the document.
        // Therefore, start at the end and work backwards
        for (int i = textFragments.size() - 1; i >= 0; i--) {
            if (parentNodeId.equals(textFragments.get(i).id())) {
                return textFragments.get(i);
            }
        }
        return null;
    }
}
