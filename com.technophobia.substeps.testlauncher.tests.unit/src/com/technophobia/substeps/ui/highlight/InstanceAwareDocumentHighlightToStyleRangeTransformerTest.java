package com.technophobia.substeps.ui.highlight;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.RGB;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.technophobia.substeps.supplier.Transformer;
import com.technophobia.substeps.ui.component.SubstepsIcon;
import com.technophobia.substeps.ui.highlight.InstanceAwareDocumentHighlightToStyleRangeTransformer;
import com.technophobia.substeps.ui.model.DocumentHighlight;
import com.technophobia.substeps.ui.model.IconHighlight;
import com.technophobia.substeps.ui.model.TextHighlight;

@RunWith(JMock.class)
public class InstanceAwareDocumentHighlightToStyleRangeTransformerTest {

    private Mockery context;

    private Transformer<TextHighlight, StyleRange> textHighlightTransformer;
    private Transformer<IconHighlight, StyleRange> iconHighlightTransformer;

    private Transformer<DocumentHighlight, StyleRange> documentHighlightTransformer;


    @SuppressWarnings("unchecked")
    @Before
    public void initialise() {
        this.context = new Mockery();

        this.textHighlightTransformer = context.mock(Transformer.class, "textHighlightTransformer");
        this.iconHighlightTransformer = context.mock(Transformer.class, "iconHighlightTransformer");

        final Map<Class<? extends DocumentHighlight>, Transformer<? extends DocumentHighlight, StyleRange>> classToDelegateTransformerMap = new HashMap<Class<? extends DocumentHighlight>, Transformer<? extends DocumentHighlight, StyleRange>>();
        classToDelegateTransformerMap.put(TextHighlight.class, textHighlightTransformer);
        classToDelegateTransformerMap.put(IconHighlight.class, iconHighlightTransformer);

        this.documentHighlightTransformer = new InstanceAwareDocumentHighlightToStyleRangeTransformer(
                classToDelegateTransformerMap);
    }


    @Test
    public void callingTransformerWithSpecificInstanceInvokesCorrectDelegate() {

        final TextHighlight textHighlight = new TextHighlight(32, 2, true, new RGB(255, 0, 0));
        final IconHighlight iconHighlight = new IconHighlight(65, 65, SubstepsIcon.SubstepPassed, 43, 12);

        final StyleRange textStyleRange = new StyleRange(32, 2, null, null);
        final StyleRange iconStyleRange = new StyleRange(65, 65, null, null);

        context.checking(new Expectations() {
            {
                oneOf(textHighlightTransformer).from(textHighlight);
                will(returnValue(textStyleRange));

                oneOf(iconHighlightTransformer).from(iconHighlight);
                will(returnValue(iconStyleRange));
            }
        });

        assertThat(documentHighlightTransformer.from(textHighlight), is(textStyleRange));
        assertThat(documentHighlightTransformer.from(iconHighlight), is(iconStyleRange));
    }


    @Test(expected = IllegalArgumentException.class)
    public void callingTransformerWithInvalidInstanceThrowsException() {

        documentHighlightTransformer.from(new FakeUnknownDocumentHighlight(54, 12));
    }

    private static final class FakeUnknownDocumentHighlight extends DocumentHighlight {

        public FakeUnknownDocumentHighlight(final int offset, final int length) {
            super(offset, length);
        }
    }
}
