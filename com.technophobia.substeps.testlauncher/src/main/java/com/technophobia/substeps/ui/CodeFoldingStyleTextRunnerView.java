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
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;

import com.technophobia.substeps.FeatureRunnerPlugin;
import com.technophobia.substeps.colour.ColourManager;
import com.technophobia.substeps.junit.ui.SubstepsIconProvider;
import com.technophobia.substeps.supplier.Transformer;
import com.technophobia.substeps.ui.component.SubstepsIcon;
import com.technophobia.substeps.ui.model.DocumentHighlight;
import com.technophobia.substeps.ui.model.StyledDocument;

public class CodeFoldingStyleTextRunnerView extends StyledTextRunnerView {

    private MappingEnabledProjectionViewer viewer;
    private ProjectionAnnotationModel annotationModel;
    private ProjectedTextPositionCalculator textPositionCalculator;

    private Annotation[] oldAnnotations;

    // Map of all highlights, keyed on offset. The reason it's keyed on
    // offset is so that new updates for a line
    // overwrite previous ones. For example, a test passed highlight should
    // replace a now processing highlight
    private final Map<Integer, DocumentHighlight> highlights;

    private final Transformer<Integer, Integer> masterToProjectedOffsetTransformer;


    public CodeFoldingStyleTextRunnerView(final ColourManager colourManager, final SubstepsIconProvider iconProvider) {
        super(colourManager, iconProvider);
        this.highlights = new HashMap<Integer, DocumentHighlight>();
        this.masterToProjectedOffsetTransformer = initMasterToProjectedOffsetTransformer();
    }


    @Override
    public void createPartControl(final Composite parent) {

        configureFolding(parent);

        super.createPartControl(parent);
    }


    @Override
    protected void resetTextTo(final StyledDocument styledDocument) {
        // order is important here: 1) Set the text, 2) update folding, 3)
        // update style ranges
        // If 2 & 3 are swapped, the folding blitzes the style ranges. That's
        // why we no longer call super.resetTextTo()
        setTextTo(styledDocument.getText());
        updateFoldingStructure(styledDocument.getPositions());
        this.highlights.clear();
        updateStyleRangesTo(styledDocument);
    }


    @Override
    protected RenderedText createRenderedText(final int offset, final RenderedText parent) {
        return new ProjectedRenderedText(true, SubstepsIcon.SubstepNoResult, offset, parent,
                masterToProjectedOffsetTransformer);
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

        this.highlights.put(Integer.valueOf(highlight.getOffset()), highlight);
    }


    protected void hideHighlightsInRange(final int start, final int length) {

        final int hiddenLine = lineNumberOfMasterOffset(start);
        final int hiddenStartLine = hiddenLine + 1;
        final int hiddenStartOffset = offsetOfMasterLineNumber(hiddenStartLine);
        final int end = start + length;

        final int projectedStart = textPositionCalculator.masterOffsetToProjectedOffset(start);

        final StyleRange[] styleRanges = viewer.getTextWidget().getStyleRanges(projectedStart,
                viewer.getTextWidget().getCharCount() - projectedStart);

        for (final StyleRange styleRange : styleRanges) {
            if (styleRange.start >= hiddenStartOffset) {
                viewer.getTextWidget().replaceStyleRanges(styleRange.start, styleRange.length, new StyleRange[0]);
            }
        }

        for (final Integer offset : highlights.keySet()) {
            final int projectedOffset = textPositionCalculator.masterOffsetToProjectedOffset(offset.intValue());
            if (offset.intValue() >= end && projectedOffset > -1) {
                final DocumentHighlight documentHighlight = highlights.get(offset);
                final StyleRange newStyleRange = documentHighlightToStyleRangeTransformer().from(documentHighlight);
                newStyleRange.start = projectedOffset;
                viewer.getTextWidget().setStyleRange(newStyleRange);
            }
        }
    }


    protected void rerunHighlightsInRange(final int start, final int length) {
        // need to use master document, not text widget
        for (final Integer offset : highlights.keySet()) {
            if (offset.intValue() > start && offset.intValue() <= start + length) {
                final DocumentHighlight highlight = highlights.get(offset);

                // if the highlight is still hidden, its projected offset will
                // be -1
                final int projectedOffset = textPositionCalculator.masterOffsetToProjectedOffset(highlight.getOffset());
                if (projectedOffset >= 0) {
                    final StyleRange newStyleRange = documentHighlightToStyleRangeTransformer().from(highlight);
                    newStyleRange.start = projectedOffset;
                    viewer.getTextWidget().setStyleRange(newStyleRange);
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

        if (oldAnnotations != null) {
            for (final Annotation oldAnnotation : oldAnnotations) {
                annotationModel.removeAnnotation(oldAnnotation);
            }
        }

        for (final Annotation newAnnotation : newAnnotations.keySet()) {
            annotationModel.addAnnotation(newAnnotation, newAnnotations.get(newAnnotation));
        }

        oldAnnotations = annotations;
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


    private Transformer<Integer, Integer> initMasterToProjectedOffsetTransformer() {
        return new Transformer<Integer, Integer>() {
            @Override
            public Integer from(final Integer from) {
                return Integer.valueOf(textPositionCalculator.masterOffsetToProjectedOffset(from.intValue()));
            }
        };
    }


    private void configureFolding(final Composite parent) {
        final IVerticalRuler ruler = new CompositeRuler();
        final IAnnotationAccess annotationAccess = new DefaultMarkerAnnotationAccess();
        final ISharedTextColors textColours = EditorsUI.getSharedTextColors();
        final IOverviewRuler overviewRuler = new OverviewRuler(annotationAccess, 10, textColours);
        viewer = new MappingEnabledProjectionViewer(parent, ruler, overviewRuler, true, SWT.V_SCROLL | SWT.H_SCROLL);
        final Document document = new Document();
        viewer.setDocument(document, new AnnotationModel());
        final ProjectionSupport projectionSupport = new ProjectionSupport(viewer, annotationAccess, textColours);
        projectionSupport.install();

        // turn projection mode on
        viewer.doOperation(ProjectionViewer.TOGGLE);

        textPositionCalculator = new ProjectedTextPositionCalculator(viewer);

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
            final int mappedStart = textPositionCalculator.masterOffsetToProjectedOffset(offset);
            final int mappedEnd = textPositionCalculator.masterOffsetToProjectedOffset(end);

            // find the offset of the next line, as when expanding,
            // the expand target has remained visible throughout
            final int lineNumber = textPositionCalculator.lineNumberOfProjectedOffset(mappedStart);
            final int nextLineOffset = textPositionCalculator.offsetOfProjectedLineNumber(lineNumber + 1);
            if (nextLineOffset < mappedEnd) {

                doIconOperation(mappedStart - 1, 2, new ExpandTextCallback());
            }
        }


        @Override
        public void textHidden(final int offset, final int length) {
            // Offset and length come in relative to source document
            // (ie fully expanded)
            final int mappedStart = textPositionCalculator.masterOffsetToProjectedOffset(offset);
            final int mappedEnd = mappedStart + length;

            // find the offset of the next line, as when collapsing,
            // we still show the icons of the collapse target, just
            // hide the nested lines
            final int lineNumber = textPositionCalculator.lineNumberOfProjectedOffset(mappedStart);
            final int nextLineOffset = textPositionCalculator.offsetOfProjectedLineNumber(lineNumber + 1);
            if (nextLineOffset < mappedEnd) {

                doIconOperation(mappedStart - 1, 2, new CollapseTextCallback());
            }
        }
    }

    private final class UpdateStyleRangesVisibilityToggle implements TextRegionVisibilityToggle {

        @Override
        public void textHidden(final int offset, final int length) {
            hideHighlightsInRange(offset, length);
        }


        @Override
        public void textVisible(final int offset, final int length) {
            rerunHighlightsInRange(offset, offset + length);
        }

    }
}
