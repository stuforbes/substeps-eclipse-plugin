package com.technophobia.substeps.ui;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.AnnotationModelEvent;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelListenerExtension;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.technophobia.substeps.ui.folding.TextRegionVisibilityToggle;
import com.technophobia.substeps.ui.folding.ToggleTextVisibilityOnCodeFoldingListener;

@RunWith(JMock.class)
public class ToggleTextVisibilityOnCodeFoldingListenerTest {

    private Mockery context;

    private TextRegionVisibilityToggle toggle;
    private IAnnotationModel annotationModel;

    private IAnnotationModelListenerExtension listener;


    @Before
    public void initialise() {
        this.context = new Mockery();

        this.toggle = context.mock(TextRegionVisibilityToggle.class);
        this.annotationModel = context.mock(IAnnotationModel.class);

        this.listener = new ToggleTextVisibilityOnCodeFoldingListener(toggle);
    }


    @Test
    public void restoresVisibilityWhenRemovingCollapsedAnnotations() {
        final ProjectionAnnotation annotation = new ProjectionAnnotation(true);
        final Position position = new Position(32, 14);

        final AnnotationModelEvent event = new AnnotationModelEvent(annotationModel);
        event.annotationRemoved(annotation, position);

        context.checking(new Expectations() {
            {
                oneOf(toggle).textVisible(32, 14);
            }
        });

        listener.modelChanged(event);
    }


    @Test
    public void noEffectWhenRemovingExpandedAnnotations() {
        final ProjectionAnnotation annotation = new ProjectionAnnotation(false);

        final AnnotationModelEvent event = new AnnotationModelEvent(annotationModel);
        event.annotationRemoved(annotation, new Position(32, 14));

        listener.modelChanged(event);
    }


    @Test
    public void hidesTextWhenAddingCollapsedAnnotation() {
        final ProjectionAnnotation annotation = new ProjectionAnnotation(true);
        final Position position = new Position(32, 14);

        final AnnotationModelEvent event = new AnnotationModelEvent(annotationModel);
        event.annotationAdded(annotation);

        context.checking(new Expectations() {
            {
                oneOf(annotationModel).getPosition(annotation);
                will(returnValue(position));

                oneOf(toggle).textHidden(32, 14);
            }
        });

        listener.modelChanged(event);
    }


    @Test
    public void noEffectWhenAddingExpandedAnnotation() {
        final ProjectionAnnotation annotation = new ProjectionAnnotation(false);

        final AnnotationModelEvent event = new AnnotationModelEvent(annotationModel);
        event.annotationAdded(annotation);

        listener.modelChanged(event);
    }


    @Test
    public void hidesTextWhenModifyingAnnotationFromExpandedToCollapsed() {
        final ProjectionAnnotation annotation = new ProjectionAnnotation(true);
        final Position position = new Position(32, 14);

        final AnnotationModelEvent event = new AnnotationModelEvent(annotationModel);
        event.annotationChanged(annotation);

        context.checking(new Expectations() {
            {
                oneOf(annotationModel).getPosition(annotation);
                will(returnValue(position));

                oneOf(toggle).textHidden(32, 14);
            }
        });

        listener.modelChanged(event);
    }


    @Test
    public void restoresVisibilityWhenModifyingAnnotationFromCollapsedToExpanded() {
        final ProjectionAnnotation annotation = new ProjectionAnnotation(false);
        final Position position = new Position(32, 14);

        final AnnotationModelEvent event = new AnnotationModelEvent(annotationModel);
        event.annotationChanged(annotation);

        context.checking(new Expectations() {
            {
                oneOf(annotationModel).getPosition(annotation);
                will(returnValue(position));

                oneOf(toggle).textVisible(32, 14);
            }
        });

        listener.modelChanged(event);
    }

}
