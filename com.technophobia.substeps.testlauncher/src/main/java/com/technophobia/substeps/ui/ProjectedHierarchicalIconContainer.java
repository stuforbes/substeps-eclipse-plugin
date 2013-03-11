package com.technophobia.substeps.ui;

import com.technophobia.substeps.supplier.Transformer;
import com.technophobia.substeps.ui.model.IconHighlight;

public class ProjectedHierarchicalIconContainer extends HierarchicalIconContainer {

    private final Transformer<Integer, Integer> masterToProjectedOffsetTransformer;


    public ProjectedHierarchicalIconContainer(final boolean isExpanded, final IconHighlight icon,
            final HierarchicalIconContainer parent,
            final Transformer<Integer, Integer> masterToProjectedOffsetTransformer) {
        super(isExpanded, icon, parent);
        this.masterToProjectedOffsetTransformer = masterToProjectedOffsetTransformer;
    }


    @Override
    public int getOffset() {
        return masterToProjectedOffsetTransformer.from(Integer.valueOf(super.getOffset()));
    }
}
