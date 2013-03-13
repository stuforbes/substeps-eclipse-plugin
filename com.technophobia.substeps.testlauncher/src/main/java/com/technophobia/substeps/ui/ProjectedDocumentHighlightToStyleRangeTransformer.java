package com.technophobia.substeps.ui;

import org.eclipse.swt.custom.StyleRange;

import com.technophobia.substeps.supplier.Transformer;
import com.technophobia.substeps.ui.model.DocumentHighlight;

public class ProjectedDocumentHighlightToStyleRangeTransformer implements Transformer<DocumentHighlight, StyleRange> {

    private final TextPositionCalculator textPositionCalculator;
    private final Transformer<DocumentHighlight, StyleRange> delegate;


    public ProjectedDocumentHighlightToStyleRangeTransformer(final TextPositionCalculator textPositionCalculator,
            final Transformer<DocumentHighlight, StyleRange> delegate) {
        this.textPositionCalculator = textPositionCalculator;
        this.delegate = delegate;
    }


    @Override
    public StyleRange from(final DocumentHighlight from) {
        final StyleRange styleRange = delegate.from(from);

        final int projectedOffset = textPositionCalculator.masterOffsetToProjectedOffset(styleRange.start);
        if (projectedOffset < 0) {
            throw new IllegalArgumentException("Could not project document highlight " + from
                    + ". The updated offset was " + projectedOffset);
        }

        styleRange.start = projectedOffset;
        return styleRange;
    }

}
