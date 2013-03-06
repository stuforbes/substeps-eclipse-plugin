package com.technophobia.substeps.ui;

import com.technophobia.eclipse.transformer.Callback1;

public class CollapseTextCallback implements Callback1<RenderedText> {

    @Override
    public void callback(final RenderedText t) {
        t.collapse();
    }

}
