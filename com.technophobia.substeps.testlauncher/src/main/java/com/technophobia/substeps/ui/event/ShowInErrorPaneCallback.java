package com.technophobia.substeps.ui.event;

import com.technophobia.eclipse.transformer.Callback1;

public class ShowInErrorPaneCallback implements Callback1<String> {

    private final Callback1<String> errorViewCallback;


    public ShowInErrorPaneCallback(final Callback1<String> errorViewCallback) {
        this.errorViewCallback = errorViewCallback;
    }


    @Override
    public void callback(final String t) {
        errorViewCallback.callback(t);
    }
}
