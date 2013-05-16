package com.technophobia.substeps.ui.highlight;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;

import com.technophobia.substeps.colour.ColourManager;
import com.technophobia.substeps.supplier.Transformer;
import com.technophobia.substeps.ui.model.TextHighlight;
import com.technophobia.substeps.ui.results.StandardTestResultsView;

public class TextHighlightToStyleRangeTransformer implements Transformer<TextHighlight, StyleRange> {

    private final ColourManager colourManager;


    public TextHighlightToStyleRangeTransformer(final ColourManager colourManager) {
        this.colourManager = colourManager;
    }


    @Override
    public StyleRange from(final TextHighlight from) {
        final StyleRange styleRange = new StyleRange(from.getOffset(), from.getLength(), colourManager.getColor(from
                .getColour()), colourManager.getColor(StandardTestResultsView.WHITE), from.isBold() ? SWT.BOLD
                : SWT.NONE);
        styleRange.underline = true;
        styleRange.underlineStyle = SWT.UNDERLINE_LINK;
        styleRange.underlineColor = colourManager.getColor(StandardTestResultsView.WHITE);
        return styleRange;
    }

}
