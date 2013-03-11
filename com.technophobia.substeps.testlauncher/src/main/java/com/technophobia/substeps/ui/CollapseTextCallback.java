package com.technophobia.substeps.ui;

import com.technophobia.eclipse.transformer.Callback1;

public class CollapseTextCallback implements Callback1<HierarchicalIconContainer> {

    @Override
    public void callback(final HierarchicalIconContainer t) {
        t.collapse();
    }

}
