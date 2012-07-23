package com.technophobia.substeps.document.content.feature.definition;

import org.eclipse.jface.text.formatter.IFormattingStrategy;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IRule;

import com.technophobia.substeps.colour.ColourManager;
import com.technophobia.substeps.document.content.feature.FeatureColour;
import com.technophobia.substeps.document.formatting.FormattingContext;
import com.technophobia.substeps.document.formatting.strategy.FixedIndentFormattingStrategy;
import com.technophobia.substeps.supplier.Supplier;

public class AndContentTypeDefinition extends AbstractFeatureContentTypeDefinition {

    public static final String CONTENT_TYPE_ID = "__feature_and";


    public AndContentTypeDefinition() {
        super(CONTENT_TYPE_ID, false);
    }


    @Override
    public IPredicateRule partitionRule() {
        return singleLineRule("And", id());
    }


    @Override
    public IRule damageRepairerRule(final ColourManager colourManager) {
        return fixedWordRule("And", colourToken(FeatureColour.PINK, colourManager));
    }


    @Override
    public IFormattingStrategy formattingStrategy(final Supplier<FormattingContext> formattingContextSupplier) {
        return new FixedIndentFormattingStrategy("\t  ");
    }
}