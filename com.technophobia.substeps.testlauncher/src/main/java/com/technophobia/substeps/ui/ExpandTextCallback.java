package com.technophobia.substeps.ui;

import com.technophobia.eclipse.transformer.Callback1;

public class ExpandTextCallback implements Callback1<RenderedText> {

    @Override
    public void callback(final RenderedText t) {
        t.expand();
    }

}
