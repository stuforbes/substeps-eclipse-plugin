package com.technophobia.substeps.ui.component;

import static com.technophobia.substeps.util.LogicOperators.not;

import java.util.ArrayList;
import java.util.List;

import com.technophobia.substeps.supplier.Callback1;
import com.technophobia.substeps.ui.session.SubstepsTestExecutionReporter;

public class SubstepsTextModelExecutionReporter implements SubstepsTestExecutionReporter {

    private final List<TextModelFragment> textFragments;

    private int currentLength;

    private final Callback1<String> textModelAsStringCallback;


    public SubstepsTextModelExecutionReporter(final Callback1<String> textModelAsStringCallback) {
        this.textModelAsStringCallback = textModelAsStringCallback;
        this.textFragments = new ArrayList<TextModelFragment>();
        this.currentLength = 0;
    }


    @Override
    public void addExecutionNode(final String id, final String parentNodeId, final String text) {
        final int depth = depthFor(id, parentNodeId);
        final int textLength = text.length() + depth;
        textFragments.add(new TextModelFragment(id, text, depth, currentLength, textLength));

        currentLength += textLength;
    }


    @Override
    public void resetExecutionState() {
        this.textFragments.clear();
    }


    @Override
    public void allExecutionNodesAdded() {
        textModelAsStringCallback.doCallback(textFragmentsAsString());
    }


    private String textFragmentsAsString() {
        final StringBuilder sb = new StringBuilder();
        for (final TextModelFragment textFragment : textFragments) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(textFragment.indentedText());
        }
        return sb.toString();
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
