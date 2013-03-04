package com.technophobia.substeps.ui.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.text.Position;

import com.technophobia.substeps.supplier.Predicate;

public class ListDelegateHierarchicalTextCollection implements HierarchicalTextCollection {

    private final List<HierarchicalTextStructure> list;


    public ListDelegateHierarchicalTextCollection() {
        this.list = new ArrayList<HierarchicalTextStructure>();
    }


    @Override
    public Iterable<HierarchicalTextStructure> items() {
        return Collections.unmodifiableList(list);
    }


    @Override
    public void add(final HierarchicalTextStructure text) {
        list.add(text);
    }


    @Override
    public void reset() {
        list.clear();
    }


    @Override
    public HierarchicalTextStructure findFirstOrNull(final Predicate<HierarchicalTextStructure> predicate) {
        for (final HierarchicalTextStructure text : list) {
            if (predicate.forModel(text)) {
                return text;
            }
        }
        return null;
    }


    @Override
    public Position positionFor(final HierarchicalTextStructure structure) {
        final int index = list.indexOf(structure);

        if (index > -1) {
            final int offset = structure.offset();

            // if this is the last item, don't check subsequent texts, as they
            // don't exist
            if (isLastItem(index)) {
                return new Position(offset, structure.length());
            } else {
                return new Position(offset, findEndOfCurrentDepth(index, offset, structure.depth()));
            }
        }

        // Couldn't find structure, return null
        return null;
    }


    private boolean isLastItem(final int index) {
        return index == list.size() - 1;
    }


    private int findEndOfCurrentDepth(final int index, final int currentDepthOffset, final int depth) {
        // find the next item that has the same depth as this structure,
        // or a lower depth
        final int nextOffsetAtDepth = findOffsetOfDepth(index + 1, depth);
        if (nextOffsetAtDepth > -1) {
            return nextOffsetAtDepth - currentDepthOffset;
        }

        // we reached the end of the list before finding a sibling or
        // parent structure
        final HierarchicalTextStructure lastItem = list.get(list.size() - 1);
        return (lastItem.offset() + lastItem.length()) - currentDepthOffset;
    }


    private int findOffsetOfDepth(final int index, final int depth) {
        final HierarchicalTextStructure structure = list.get(index);
        if (structure.depth() <= depth) {
            return structure.offset();
        } else if (isLastItem(index)) {
            return -1;
        } else {
            return findOffsetOfDepth(index + 1, depth);
        }
    }
}
