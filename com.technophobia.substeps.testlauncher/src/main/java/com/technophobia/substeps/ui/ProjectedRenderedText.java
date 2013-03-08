package com.technophobia.substeps.ui;

import com.technophobia.substeps.supplier.Transformer;
import com.technophobia.substeps.ui.component.SubstepsIcon;

public class ProjectedRenderedText extends RenderedText {

    private final Transformer<Integer, Integer> masterToProjectedOffsetTransformer;


    public ProjectedRenderedText(final boolean isExpanded, final SubstepsIcon icon, final int offset,
            final RenderedText parent, final Transformer<Integer, Integer> masterToProjectedOffsetTransformer) {
        super(isExpanded, icon, offset, parent);
        this.masterToProjectedOffsetTransformer = masterToProjectedOffsetTransformer;
    }


    @Override
    public int getOffset() {
        return masterToProjectedOffsetTransformer.from(Integer.valueOf(super.getOffset()));
    }


    public int getMasterOffset() {
        return super.getOffset();
    }
}
