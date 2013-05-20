package com.technophobia.substeps.ui.event;

import com.technophobia.eclipse.transformer.Callback1;
import com.technophobia.eclipse.transformer.Locator;
import com.technophobia.substeps.ui.component.TextModelFragment;

public class ShowInErrorPaneCallback implements LineClickHandler {

    private final Callback1<String> errorViewCallback;
    private final Locator<TextModelFragment, Integer> textModelFragmentAtOffsetLocator;


    public ShowInErrorPaneCallback(final Callback1<String> errorViewCallback,
            final Locator<TextModelFragment, Integer> textModelFragmentAtOffsetLocator) {
        this.errorViewCallback = errorViewCallback;
        this.textModelFragmentAtOffsetLocator = textModelFragmentAtOffsetLocator;
    }


    @Override
    public void onLineClick(final int offset, final String line) {
        final TextModelFragment textModelFragment = textModelFragmentAtOffsetLocator.one(Integer.valueOf(offset));
        if (textModelFragment != null) {
            if (textModelFragment.isFailure()) {
                errorViewCallback.callback(textModelFragment.failureOrNull().formattedMessage());
            } else if (textModelFragment.isError()) {
                errorViewCallback.callback(textModelFragment.errorOrNull().formattedMessage());
            }
        }
    }
}
