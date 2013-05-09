package com.technophobia.substeps.ui.action;

import org.eclipse.jface.action.Action;

import com.technophobia.substeps.junit.ui.SubstepsFeatureMessages;
import com.technophobia.substeps.junit.ui.SubstepsIconProvider;
import com.technophobia.substeps.supplier.Callback;
import com.technophobia.substeps.ui.component.SubstepsIcon;

public class ShowErrorsAction extends Action {

    private final Callback onPressed;
    private final Callback onDepressed;


    public ShowErrorsAction(final Callback onPressed, final Callback onDepressed,
            final SubstepsIconProvider iconProvider) {
        super(SubstepsFeatureMessages.ActionShowErrorsLabel);
        this.onPressed = onPressed;
        this.onDepressed = onDepressed;

        setToolTipText(SubstepsFeatureMessages.ActionShowErrorsTooltip);
        setImageDescriptor(iconProvider.imageDescriptorFor(SubstepsIcon.ActionShowErrors));
        setChecked(false);
    }


    @Override
    public void run() {
        if (isChecked()) {
            onPressed.doCallback();
        } else {
            onDepressed.doCallback();
        }
    }
}
