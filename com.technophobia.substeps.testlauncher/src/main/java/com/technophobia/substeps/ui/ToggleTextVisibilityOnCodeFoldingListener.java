package com.technophobia.substeps.ui;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModelEvent;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.jface.text.source.IAnnotationModelListenerExtension;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;

import com.technophobia.substeps.supplier.Transformer;

public class ToggleTextVisibilityOnCodeFoldingListener implements IAnnotationModelListener,
        IAnnotationModelListenerExtension {

    private final TextRegionVisibilityToggle textRegionVisibilityToggle;


    public ToggleTextVisibilityOnCodeFoldingListener(final TextRegionVisibilityToggle textRegionVisibilityToggle) {
        this.textRegionVisibilityToggle = textRegionVisibilityToggle;
    }


    @Override
    public void modelChanged(final AnnotationModelEvent event) {
        final Transformer<Annotation, Position> annotationModelPositionTransformer = annotationModelPositionTransformer(event
                .getAnnotationModel());

        handleRemovals(event.getRemovedAnnotations(), removedAnnotationPositionTransformer(event));
        handleAdditions(event.getAddedAnnotations(), annotationModelPositionTransformer);
        handleChanges(event.getChangedAnnotations(), annotationModelPositionTransformer);
    }


    @Override
    public void modelChanged(final IAnnotationModel model) {
        // No-op
    }


    private void handleRemovals(final Annotation[] removedAnnotations,
            final Transformer<Annotation, Position> positionTransformer) {

        // reverse order, so we deal with the end annotations 1st. That way,
        // text operations (addition/removal) etc don't affect subsequent
        // annotations
        for (int i = removedAnnotations.length - 1; i >= 0; i--) {
            final Annotation removedAnnotation = removedAnnotations[i];
            if (removedAnnotation instanceof ProjectionAnnotation) {
                if (((ProjectionAnnotation) removedAnnotation).isCollapsed()) {
                    final Position position = positionTransformer.from(removedAnnotation);
                    if (position != null) {
                        textRegionVisibilityToggle.textVisible(position.getOffset(), position.getLength());
                    }
                }
            }
        }
    }


    private void handleAdditions(final Annotation[] addedAnnotations,
            final Transformer<Annotation, Position> positionTransformer) {
        // reverse order, so we deal with the end annotations 1st. That way,
        // text operations (addition/removal) etc don't affect subsequent
        // annotations
        for (int i = addedAnnotations.length - 1; i >= 0; i--) {
            final Annotation addedAnnotation = addedAnnotations[i];
            if (addedAnnotation instanceof ProjectionAnnotation) {
                if (((ProjectionAnnotation) addedAnnotation).isCollapsed()) {
                    final Position position = positionTransformer.from(addedAnnotation);
                    if (position != null) {
                        textRegionVisibilityToggle.textHidden(position.getOffset(), position.getLength());
                    }
                }
            }
        }
    }


    private void handleChanges(final Annotation[] changedAnnotations,
            final Transformer<Annotation, Position> positionTransformer) {

        // We're making the assumption here that the only modifications that can
        // occur on annotations is their collapsed state changes. This may prove
        // to be wrong later

        // reverse order, so we deal with the end annotations 1st. That way,
        // text operations (addition/removal) etc don't affect subsequent
        // annotations
        for (int i = changedAnnotations.length - 1; i >= 0; i--) {
            final Annotation changedAnnotation = changedAnnotations[i];
            if (changedAnnotation instanceof ProjectionAnnotation) {

                final Position position = positionTransformer.from(changedAnnotation);
                if (position != null) {
                    if (((ProjectionAnnotation) changedAnnotation).isCollapsed()) {
                        textRegionVisibilityToggle.textHidden(position.getOffset(), position.getLength());
                    } else {
                        textRegionVisibilityToggle.textVisible(position.getOffset(), position.getLength());
                    }
                }
            }
        }
    }


    private Transformer<Annotation, Position> removedAnnotationPositionTransformer(final AnnotationModelEvent event) {
        return new Transformer<Annotation, Position>() {
            @Override
            public Position from(final Annotation annotation) {
                return event.getPositionOfRemovedAnnotation(annotation);
            }
        };
    }


    private Transformer<Annotation, Position> annotationModelPositionTransformer(final IAnnotationModel annotationModel) {
        return new Transformer<Annotation, Position>() {
            @Override
            public Position from(final Annotation annotation) {
                return annotationModel.getPosition(annotation);
            }
        };
    }
}
