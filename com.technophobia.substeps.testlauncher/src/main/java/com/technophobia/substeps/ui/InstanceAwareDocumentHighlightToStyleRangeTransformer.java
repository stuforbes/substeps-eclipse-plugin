package com.technophobia.substeps.ui;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.custom.StyleRange;

import com.technophobia.substeps.colour.ColourManager;
import com.technophobia.substeps.supplier.Transformer;
import com.technophobia.substeps.ui.model.DocumentHighlight;
import com.technophobia.substeps.ui.model.IconHighlight;
import com.technophobia.substeps.ui.model.TextHighlight;

public class InstanceAwareDocumentHighlightToStyleRangeTransformer implements
        Transformer<DocumentHighlight, StyleRange> {

    private final Map<Class<? extends DocumentHighlight>, Transformer<? extends DocumentHighlight, StyleRange>> classToDelegateTransformerMap;


    public InstanceAwareDocumentHighlightToStyleRangeTransformer(final ColourManager colourManager) {
        this(defaultClassToDelegateTransformerMap(colourManager));
    }


    public InstanceAwareDocumentHighlightToStyleRangeTransformer(
            final Map<Class<? extends DocumentHighlight>, Transformer<? extends DocumentHighlight, StyleRange>> classToDelegateTransformerMap) {
        this.classToDelegateTransformerMap = classToDelegateTransformerMap;
    }


    @SuppressWarnings("unchecked")
    @Override
    public StyleRange from(final DocumentHighlight from) {

        final Class<? extends DocumentHighlight> highlightClass = from.getClass();
        if (classToDelegateTransformerMap.containsKey(highlightClass)) {
            final Transformer<DocumentHighlight, StyleRange> delegate = (Transformer<DocumentHighlight, StyleRange>) classToDelegateTransformerMap
                    .get(highlightClass);
            return delegate.from(from);
        }

        throw new IllegalArgumentException("Could not find StyleRange Transformer for DocumentHighlight of type "
                + from.getClass().getName());
    }


    private static Map<Class<? extends DocumentHighlight>, Transformer<? extends DocumentHighlight, StyleRange>> defaultClassToDelegateTransformerMap(
            final ColourManager colourManager) {
        final Map<Class<? extends DocumentHighlight>, Transformer<? extends DocumentHighlight, StyleRange>> results = new HashMap<Class<? extends DocumentHighlight>, Transformer<? extends DocumentHighlight, StyleRange>>();
        results.put(TextHighlight.class, new TextHighlightToStyleRangeTransformer(colourManager));
        results.put(IconHighlight.class, new IconHighlightToStyleRangeTransformer());
        return Collections.unmodifiableMap(results);
    }
}
