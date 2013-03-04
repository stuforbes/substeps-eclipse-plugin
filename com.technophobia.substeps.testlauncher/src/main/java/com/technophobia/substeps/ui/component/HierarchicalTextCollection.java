package com.technophobia.substeps.ui.component;

import org.eclipse.jface.text.Position;

import com.technophobia.substeps.supplier.Predicate;

public interface HierarchicalTextCollection {

    void add(HierarchicalTextStructure text);


    void reset();


    Iterable<HierarchicalTextStructure> items();


    HierarchicalTextStructure findFirstOrNull(Predicate<HierarchicalTextStructure> predicate);


    Position positionFor(HierarchicalTextStructure structure);
}
