package com.technophobia.substeps.ui;

import org.eclipse.ui.IWorkbenchPartSite;

import com.technophobia.eclipse.transformer.Callback1;
import com.technophobia.substeps.colour.ColourManager;
import com.technophobia.substeps.junit.ui.SubstepsIconProvider;
import com.technophobia.substeps.ui.results.CodeFoldingTestResultsView;
import com.technophobia.substeps.ui.results.StandardTestResultsView;

public class CodeFoldingStyleTextRunnerView extends StyledTextRunnerView {

    public CodeFoldingStyleTextRunnerView(final ColourManager colourManager, final SubstepsIconProvider iconProvider,
            final IWorkbenchPartSite site) {
        super(colourManager, iconProvider, site);
    }


    @Override
    protected StandardTestResultsView createTestResultsView(final IWorkbenchPartSite site,
            final SubstepsIconProvider iconProvider, final ColourManager colourManager,
            final Callback1<String> errorViewCallback) {
        return new CodeFoldingTestResultsView(site, iconProvider, colourManager, errorViewCallback,
                textModelFragmentAtOffsetLocator());
    }

}
