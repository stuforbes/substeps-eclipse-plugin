package com.technophobia.substeps.ui;

import com.technophobia.eclipse.transformer.Callback1;

public class ExpandTextCallback implements Callback1<HierarchicalIconContainer> {

    @Override
    public void callback(final HierarchicalIconContainer t) {
        t.expand();
    }

}
