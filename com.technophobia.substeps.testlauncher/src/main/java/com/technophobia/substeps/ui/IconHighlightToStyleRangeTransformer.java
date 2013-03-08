package com.technophobia.substeps.ui;

import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.GlyphMetrics;

import com.technophobia.substeps.supplier.Transformer;
import com.technophobia.substeps.ui.model.IconHighlight;

public class IconHighlightToStyleRangeTransformer implements Transformer<IconHighlight, StyleRange> {

    @Override
    public StyleRange from(final IconHighlight from) {
        final StyleRange style = new StyleRange();
        style.start = from.getOffset();
        style.length = 1;
        style.metrics = new GlyphMetrics(from.getImageHeight(), 0, from.getImageWidth());
        return style;
    }
}
