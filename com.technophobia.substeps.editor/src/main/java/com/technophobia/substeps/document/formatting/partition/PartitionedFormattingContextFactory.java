/*******************************************************************************
 * Copyright Technophobia Ltd 2012
 * 
 * This file is part of the Substeps Eclipse Plugin.
 * 
 * The Substeps Eclipse Plugin is free software: you can redistribute it and/or modify
 * it under the terms of the Eclipse Public License v1.0.
 * 
 * The Substeps Eclipse Plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Eclipse Public License for more details.
 * 
 * You should have received a copy of the Eclipse Public License
 * along with the Substeps Eclipse Plugin.  If not, see <http://www.eclipse.org/legal/epl-v10.html>.
 ******************************************************************************/
package com.technophobia.substeps.document.formatting.partition;

import org.eclipse.jface.text.TypedPosition;

import com.technophobia.substeps.document.content.ContentTypeDefinitionFactory;
import com.technophobia.substeps.document.formatting.FormattingContext;
import com.technophobia.substeps.document.formatting.FormattingContextFactory;

/**
 * Creates a new {@link PartitionedFormattingContext}
 * 
 * @author sforbes
 * 
 */
public class PartitionedFormattingContextFactory implements FormattingContextFactory {

    private final ContentTypeDefinitionFactory contentTypeDefinitionFactory;


    public PartitionedFormattingContextFactory(final ContentTypeDefinitionFactory contentTypeDefinitionFactory) {
        this.contentTypeDefinitionFactory = contentTypeDefinitionFactory;

    }


    @Override
    public FormattingContext createFor(final TypedPosition[] positions, final int currentPosition) {
        return new PartitionedFormattingContext(positions, currentPosition, contentTypeDefinitionFactory);
    }

}
