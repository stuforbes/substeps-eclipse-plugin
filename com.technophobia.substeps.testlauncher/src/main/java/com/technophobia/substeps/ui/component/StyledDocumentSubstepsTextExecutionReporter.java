package com.technophobia.substeps.ui.component;

import static com.technophobia.substeps.util.LogicOperators.not;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.Position;

import com.technophobia.substeps.FeatureRunnerPlugin;
import com.technophobia.substeps.supplier.Predicate;
import com.technophobia.substeps.ui.TextHighlighter;
import com.technophobia.substeps.ui.session.SubstepsTestExecutionReporter;

public class StyledDocumentSubstepsTextExecutionReporter implements SubstepsTestExecutionReporter {

    private static final String NULL_PARENT_ID = "-1";

    private int currentLength;
    private int currentLine;

    private final HierarchicalTextCollection textFragments;

    private final HierarchicalTextStructureFactory textStructureFactory;

    private final TextHighlighter highlighter;


    public StyledDocumentSubstepsTextExecutionReporter(final HierarchicalTextCollection textCollection,
            final HierarchicalTextStructureFactory textStructureFactory, final TextHighlighter highlighter) {
        this.textFragments = textCollection;
        this.textStructureFactory = textStructureFactory;
        this.highlighter = highlighter;
        this.currentLength = 0;
        this.currentLine = 0;
    }


    @Override
    public void addExecutionNode(final String id, final String parentNodeId, final String text) {
        // We don't add the root 'Features' node
        if (not(NULL_PARENT_ID.equals(parentNodeId))) {
            final HierarchicalTextStructure fragment = textStructureFactory.createTextStructureFor(currentLength,
                    currentLine, id, parentNodeId, text);

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

            highlighter.highlight(textFragment);
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
        this.textFragments.reset();
        this.currentLength = 0;
        this.currentLine = 0;
    }


    @Override
    public void allExecutionNodesAdded() {
        final StringBuilder sb = new StringBuilder();
        final List<Position> positions = new ArrayList<Position>();

        final Iterable<HierarchicalTextStructure> items = textFragments.items();
        for (final HierarchicalTextStructure textStructure : items) {
            final TextModelFragment textFragment = (TextModelFragment) textStructure;
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(textFragment.indentedText());

            // only parent fragments are 'foldable'
            if (textFragment.hasChildren()) {
                positions.add(textFragments.positionFor(textStructure));
            }
        }
        highlighter.documentChanged(sb.toString(), items, positions);
    }


    private TextModelFragment findNodeWithIdOrNull(final String id) {
        return (TextModelFragment) textFragments.findFirstOrNull(new Predicate<HierarchicalTextStructure>() {
            @Override
            public boolean forModel(final HierarchicalTextStructure text) {
                return id.equals(((TextModelFragment) text).id());
            }
        });
    }
}
