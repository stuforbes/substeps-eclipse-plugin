package com.technophobia.substeps.ui;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.RGB;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.technophobia.substeps.supplier.Transformer;
import com.technophobia.substeps.ui.folding.TextPositionCalculator;
import com.technophobia.substeps.ui.highlight.ProjectedDocumentHighlightToStyleRangeTransformer;
import com.technophobia.substeps.ui.model.DocumentHighlight;
import com.technophobia.substeps.ui.model.TextHighlight;

@RunWith(JMock.class)
public class ProjectedDocumentHighlightToStyleRangeTransformerTest {

    private Mockery context;

    private TextPositionCalculator textPositionCalculator;
    private Transformer<DocumentHighlight, StyleRange> delegateTransformer;

    private Transformer<DocumentHighlight, StyleRange> projectedTransformer;


    @SuppressWarnings("unchecked")
    @Before
    public void initialise() {
        this.context = new Mockery();

        this.textPositionCalculator = context.mock(TextPositionCalculator.class);
        this.delegateTransformer = context.mock(Transformer.class, "delegateTransformer");

        this.projectedTransformer = new ProjectedDocumentHighlightToStyleRangeTransformer(textPositionCalculator,
                delegateTransformer);
    }


    @Test
    public void updatesStyleRangeFromDelegateWithProjectedOffset() {

        final DocumentHighlight highlight = new TextHighlight(2, 5, new RGB(32, 12, 43));
        final StyleRange expectedStyleRange = new StyleRange();
        expectedStyleRange.start = 2;

        context.checking(new Expectations() {
            {
                oneOf(delegateTransformer).from(highlight);
                will(returnValue(expectedStyleRange));

                oneOf(textPositionCalculator).masterOffsetToProjectedOffset(2);
                will(returnValue(5));
            }
        });

        final StyleRange styleRange = projectedTransformer.from(highlight);
        assertThat(styleRange.start, is(5));
    }


    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionIfCannotBeProjected() {

        final DocumentHighlight highlight = new TextHighlight(2, 5, new RGB(32, 12, 43));
        final StyleRange expectedStyleRange = new StyleRange();
        expectedStyleRange.start = 2;

        context.checking(new Expectations() {
            {
                oneOf(delegateTransformer).from(highlight);
                will(returnValue(expectedStyleRange));

                oneOf(textPositionCalculator).masterOffsetToProjectedOffset(2);
                will(returnValue(-1));
            }
        });

        projectedTransformer.from(highlight);
    }
}
