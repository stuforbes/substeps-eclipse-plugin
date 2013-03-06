package com.technophobia.substeps.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.BadLocationException;
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

import com.technophobia.eclipse.transformer.Callback1;
import com.technophobia.substeps.FeatureRunnerPlugin;
import com.technophobia.substeps.colour.ColourManager;
import com.technophobia.substeps.junit.ui.SubstepsIconProvider;
import com.technophobia.substeps.ui.model.DocumentHighlight;
import com.technophobia.substeps.ui.model.StyledDocument;

public class CodeFoldingStyleTextRunnerView extends StyledTextRunnerView {

    private MappingEnabledProjectionViewer viewer;
    private ProjectionAnnotationModel annotationModel;

    private Annotation[] oldAnnotations;

    // Map of all highlights, keyed on line number. The reason it's keyed on
    // line number is so that new updates for a line
    // overwrite previous ones. For example, a test passed highlight should
    // replace a now processing highlight
    private final Map<Integer, DocumentHighlight> highlights;


    public CodeFoldingStyleTextRunnerView(final ColourManager colourManager, final SubstepsIconProvider iconProvider) {
        super(colourManager, iconProvider);
        this.highlights = new HashMap<Integer, DocumentHighlight>();
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
        this.highlights.clear();
    }


    @Override
    protected StyledText createTextComponent(final Composite parent) {
        return viewer.getTextWidget();
    }


    @Override
    protected void setTextTo(final String text) {
        viewer.getDocument().set(text);
    }


    @Override
    protected void addHighlight(final DocumentHighlight highlight) {
        super.addHighlight(highlight);

        this.highlights.put(Integer.valueOf(highlight.getLine()), highlight);
    }


    protected void rerunHighlightsInRange(final int start, final int end) {
        // need to use master document, not text widget
        final int startLine = lineNumberOfMasterOffset(start);
        final int endLine = lineNumberOfMasterOffset(end - 1);
        for (final DocumentHighlight highlight : highlights.values()) {
            if (highlight.getLine() >= startLine && highlight.getLine() <= endLine) {
                final int projectedLineNumber = masterLineToProjectedLine(highlight.getLine());
                if (projectedLineNumber >= 0) {
                    super.addHighlight(new DocumentHighlight(projectedLineNumber, highlight.getLength(), highlight
                            .getColour()));
                }
            }
        }
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


    protected int masterOffsetToProjectedOffset(final int masterOffset) {
        return viewer.projectedOffset(masterOffset);
    }


    protected int masterLineToProjectedLine(final int masterLine) {
        return viewer.projectedLineNumber(masterLine);
    }


    protected int lineNumberOfProjectedOffset(final int offset) {
        return viewer.getTextWidget().getLineAtOffset(offset);
    }


    protected int offsetOfProjectedLineNumber(final int lineNumber) {
        return viewer.getTextWidget().getOffsetAtLine(lineNumber);
    }


    protected int lineNumberOfMasterOffset(final int offset) {
        try {
            return viewer.getDocument().getLineOfOffset(offset);
        } catch (final BadLocationException ex) {
            FeatureRunnerPlugin.log(IStatus.WARNING, "Could not get line number for offset " + offset
                    + ", returning -1");
            return -1;
        }
    }


    protected int offsetOfMasterLineNumber(final int lineNumber) {
        try {
            return viewer.getDocument().getLineOffset(lineNumber);
        } catch (final BadLocationException e) {
            FeatureRunnerPlugin.log(IStatus.WARNING, "Could not get offset for line number" + lineNumber
                    + ", returning -1");
            return -1;
        }
    }


    private void configureFolding(final Composite parent) {
        final IVerticalRuler ruler = new CompositeRuler();
        final IAnnotationAccess annotationAccess = new DefaultMarkerAnnotationAccess();
        final ISharedTextColors textColours = EditorsUI.getSharedTextColors();
        final IOverviewRuler overviewRuler = new OverviewRuler(annotationAccess, 10, textColours);
        viewer = new MappingEnabledProjectionViewer(parent, ruler, overviewRuler, true, SWT.NONE);
        final Document document = new Document();
        viewer.setDocument(document, new AnnotationModel());
        final ProjectionSupport projectionSupport = new ProjectionSupport(viewer, annotationAccess, textColours);
        projectionSupport.install();

        // turn projection mode on
        viewer.doOperation(ProjectionViewer.TOGGLE);

        annotationModel = viewer.getProjectionAnnotationModel();

        final TextRegionVisibilityToggle toggle = new CompositeVisiblityToggle(
                new UpdateRenderedTextVisibilityToggle(), //
                new UpdateStyleRangesVisibilityToggle() //
        );

        annotationModel.addAnnotationModelListener(new ToggleTextVisibilityOnCodeFoldingListener(toggle));
    }

    private final class UpdateRenderedTextVisibilityToggle implements TextRegionVisibilityToggle {

        @Override
        public void textVisible(final int offset, final int length) {
            final int end = offset + length;

            // Offset and length come in relative to source document
            // (ie fully expanded)
            final int mappedStart = masterOffsetToProjectedOffset(offset);
            final int mappedEnd = masterOffsetToProjectedOffset(end);

            // find the offset of the next line, as when expanding,
            // the expand target has remained visible throughout
            final int lineNumber = lineNumberOfProjectedOffset(mappedStart);
            final int nextLineOffset = offsetOfProjectedLineNumber(lineNumber + 1);
            if (nextLineOffset < mappedEnd) {

                doIconOperation(nextLineOffset, new Callback1<RenderedText>() {
                    @Override
                    public void callback(final RenderedText t) {
                        if (t.isRendered() && t.getOffset() > mappedEnd) {
                            t.transposeBy(mappedEnd - nextLineOffset);
                        }
                    }
                });

                doIconOperation(mappedStart - 1, 2, new ExpandTextCallback());
            }
        }


        @Override
        public void textHidden(final int offset, final int length) {
            // Offset and length come in relative to source document
            // (ie fully expanded)
            final int mappedStart = masterOffsetToProjectedOffset(offset);
            final int mappedEnd = mappedStart + length;

            // find the offset of the next line, as when collapsing,
            // we still show the icons of the collapse target, just
            // hide the nested lines
            final int lineNumber = lineNumberOfProjectedOffset(mappedStart);
            final int nextLineOffset = offsetOfProjectedLineNumber(lineNumber + 1);
            if (nextLineOffset < mappedEnd) {

                doIconOperation(mappedStart - 1, 2, new CollapseTextCallback());

                doIconOperation(mappedStart, new Callback1<RenderedText>() {
                    @Override
                    public void callback(final RenderedText t) {
                        if (t.getOffset() >= mappedEnd) {
                            t.transposeBy(-1 * (mappedEnd - nextLineOffset));
                        }
                    }
                });
            }
        }
    }

    private final class UpdateStyleRangesVisibilityToggle implements TextRegionVisibilityToggle {

        @Override
        public void textHidden(final int offset, final int length) {
            // No-op
        }


        @Override
        public void textVisible(final int offset, final int length) {
            rerunHighlightsInRange(offset, offset + length);
        }

    }
}
