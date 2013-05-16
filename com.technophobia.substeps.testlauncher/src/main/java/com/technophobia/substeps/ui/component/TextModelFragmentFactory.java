package com.technophobia.substeps.ui.component;

import com.technophobia.substeps.supplier.Predicate;
import com.technophobia.substeps.ui.highlight.TextHighlighter;

public class TextModelFragmentFactory implements HierarchicalTextStructureFactory {

    private final HierarchicalTextCollection textCollection;
    private final TextHighlighter stateChangeHighlighter;


    public TextModelFragmentFactory(final HierarchicalTextCollection textCollection,
            final TextHighlighter stateChangeHighlighter) {
        this.textCollection = textCollection;
        this.stateChangeHighlighter = stateChangeHighlighter;
    }


    @Override
    public HierarchicalTextStructure createTextStructureFor(final int currentPosition, final int currentLine,
            final String id, final String parentId, final String text) {
        final HierarchicalTextStructure parent = textCollection
                .findFirstOrNull(new Predicate<HierarchicalTextStructure>() {
                    @Override
                    public boolean forModel(final HierarchicalTextStructure t) {
                        final TextModelFragment fragment = (TextModelFragment) t;
                        return parentId.equals(fragment.id());
                    }
                });

        if (parent == null) {
            return TextModelFragment.createRootFragment(id, text, currentPosition, currentLine, stateChangeHighlighter);
        }
        return ((TextModelFragment) parent).createChild(id, text, currentPosition, currentLine);
    }
}
