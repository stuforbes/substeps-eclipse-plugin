package com.technophobia.substeps.ui;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentInformationMapping;
import org.eclipse.jface.text.projection.ProjectionDocument;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.widgets.Composite;

public class MappingEnabledProjectionViewer extends ProjectionViewer {

    public MappingEnabledProjectionViewer(final Composite parent, final IVerticalRuler ruler,
            final IOverviewRuler overviewRuler, final boolean showsAnnotationOverview, final int styles) {
        super(parent, ruler, overviewRuler, showsAnnotationOverview, styles);
    }


    public int projectedOffset(final int masterDocumentOffset) {
        final IDocumentInformationMapping infoMapping = infoMapping();
        try {
            return infoMapping.toImageOffset(masterDocumentOffset);
        } catch (final BadLocationException ex) {
            throw new IllegalStateException(
                    "Could not get offset in projected document corresponding to master document offset "
                            + masterDocumentOffset, ex);
        }
    }


    public int projectedLineNumber(final int masterDocumentLine) {
        final IDocumentInformationMapping infoMapping = infoMapping();
        try {
            return infoMapping.toImageLine(masterDocumentLine);
        } catch (final BadLocationException ex) {
            throw new IllegalStateException(
                    "Could not get line in projected document corresponding to master document line "
                            + masterDocumentLine, ex);
        }
    }


    public int masterDocumentOffset(final int projectedOffset) {
        final IDocumentInformationMapping infoMapping = infoMapping();
        try {
            return infoMapping.toOriginOffset(projectedOffset);
        } catch (final BadLocationException ex) {
            throw new IllegalStateException(
                    "Could not get offset in master document corresponding to projected offset " + projectedOffset, ex);
        }
    }


    public int masterDocumentLineNumber(final int projectedLine) {
        final IDocumentInformationMapping infoMapping = infoMapping();
        try {
            return infoMapping.toOriginLine(projectedLine);
        } catch (final BadLocationException ex) {
            throw new IllegalStateException("Could not get line in master document corresponding to projected line "
                    + projectedLine, ex);
        }
    }


    private IDocumentInformationMapping infoMapping() {
        final IDocument document = getVisibleDocument();
        if (document instanceof ProjectionDocument) {
            return ((ProjectionDocument) document).getDocumentInformationMapping();
        }
        return null;
    }
}