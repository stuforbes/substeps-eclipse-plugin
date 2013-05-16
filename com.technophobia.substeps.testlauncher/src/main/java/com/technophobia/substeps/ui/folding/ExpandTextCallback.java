package com.technophobia.substeps.ui.folding;

import com.technophobia.eclipse.transformer.Callback1;
import com.technophobia.substeps.ui.model.HierarchicalIconContainer;

public class ExpandTextCallback implements Callback1<HierarchicalIconContainer> {

    @Override
    public void callback(final HierarchicalIconContainer t) {
        t.expand();
    }

}
