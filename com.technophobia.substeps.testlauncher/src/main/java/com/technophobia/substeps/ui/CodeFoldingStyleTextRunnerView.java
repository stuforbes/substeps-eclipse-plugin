package com.technophobia.substeps.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.OverviewRuler;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;

import com.technophobia.substeps.colour.ColourManager;
import com.technophobia.substeps.junit.ui.SubstepsIconProvider;
import com.technophobia.substeps.ui.model.StyledDocument;

public class CodeFoldingStyleTextRunnerView extends StyledTextRunnerView {

    private ProjectionViewer viewer;
    private ProjectionAnnotationModel annotationModel;

    private Annotation[] oldAnnotations;


    public CodeFoldingStyleTextRunnerView(final ColourManager colourManager, final SubstepsIconProvider iconProvider) {
        super(colourManager, iconProvider);
    }


    @Override
    public void createPartControl(final Composite parent) {

        configureFolding(parent);

        super.createPartControl(parent);
    }


    @Override
    protected void resetTextTo(final StyledDocument styledDocument) {
        super.resetTextTo(styledDocument);
        updateFoldingStructure(styledDocument.getPositions());
    }


    @Override
    protected StyledText createTextComponent(final Composite parent) {
        return viewer.getTextWidget();
    }


    @Override
    protected void setTextTo(final String text) {
        viewer.getDocument().set(text);
    }


    protected void updateFoldingStructure(final List<Position> positions) {
        final Annotation[] annotations = new Annotation[positions.size()];

        // this will hold the new annotations along // with their corresponding
        // position
        final Map<Annotation, Position> newAnnotations = new HashMap<Annotation, Position>();

        for (int i = 0; i < positions.size(); i++) {
            final Annotation annotation = new ProjectionAnnotation();

            newAnnotations.put(annotation, positions.get(i));

            annotations[i] = annotation;
        }

        annotationModel.modifyAnnotations(oldAnnotations, newAnnotations, null);

        oldAnnotations = annotations;
    }


    private void configureFolding(final Composite parent) {
        final IVerticalRuler ruler = new CompositeRuler();
        final IAnnotationAccess annotationAccess = new DefaultMarkerAnnotationAccess();
        final ISharedTextColors textColours = EditorsUI.getSharedTextColors();
        final IOverviewRuler overviewRuler = new OverviewRuler(annotationAccess, 10, textColours);
        viewer = new ProjectionViewer(parent, ruler, overviewRuler, true, SWT.NONE);
        final Document document = new Document();
        viewer.setDocument(document, new AnnotationModel());
        final ProjectionSupport projectionSupport = new ProjectionSupport(viewer, annotationAccess, textColours);
        projectionSupport.install();

        // turn projection mode on
        viewer.doOperation(ProjectionViewer.TOGGLE);

        annotationModel = viewer.getProjectionAnnotationModel();
    }
}
