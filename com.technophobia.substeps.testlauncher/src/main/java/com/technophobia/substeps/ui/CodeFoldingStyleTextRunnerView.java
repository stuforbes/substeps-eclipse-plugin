package com.technophobia.substeps.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IAnnotationModelListener;
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
import com.technophobia.substeps.ui.component.StyledDocumentUpdater.HighlightEvent;
import com.technophobia.substeps.ui.model.DocumentHighlight;
import com.technophobia.substeps.ui.model.IconHighlight;
import com.technophobia.substeps.ui.model.StyledDocument;

public class CodeFoldingStyleTextRunnerView extends StyledTextRunnerView {

    private MappingEnabledProjectionViewer viewer;
    private ProjectionAnnotationModel annotationModel;
    private TextPositionCalculator textPositionCalculator;

    private final Map<Integer, Annotation> oldAnnotationsByMasterOffset;

    // Map of all highlights, keyed on offset. The reason it's keyed on
    // offset is so that new updates for a line
    // overwrite previous ones. For example, a test passed highlight should
    // replace a now processing highlight
    private final Map<Integer, DocumentHighlight> highlights;
    private final Transformer<Integer, Integer> masterToProjectedOffsetTransformer;
    private IAnnotationModelListener annotationModelListener;


    public CodeFoldingStyleTextRunnerView(final ColourManager colourManager, final SubstepsIconProvider iconProvider) {
        super(colourManager, iconProvider);
        this.highlights = new HashMap<Integer, DocumentHighlight>();
        this.masterToProjectedOffsetTransformer = initMasterToProjectedOffsetTransformer();
        this.oldAnnotationsByMasterOffset = new HashMap<Integer, Annotation>();
    }


    @Override
    public void createPartControl(final Composite parent) {

        configureFolding(parent);

        super.createPartControl(parent);
    }


    @Override
    public void dispose() {
        this.highlights.clear();
        this.oldAnnotationsByMasterOffset.clear();

        this.viewer = null;
        this.annotationModel = null;
        super.dispose();
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
    protected StyledText createTextComponent(final Composite parent) {
        return viewer.getTextWidget();
    }


    @Override
    protected void setTextTo(final String text) {
        viewer.getTextWidget().setStyleRange(null);
        viewer.getDocument().set(text);
    }


    @Override
    protected void clearText() {
        resetFoldedDocument();
        super.clearText();
    }


    @Override
    protected void addHighlight(final DocumentHighlight highlight) {
        super.addHighlight(highlight);

        this.highlights.put(Integer.valueOf(highlight.getOffset()), highlight);
    }


    protected void hideHighlightsInRange(final int start, final int length) {

        final int hiddenLine = textPositionCalculator.masterLineToProjectedLine(textPositionCalculator
                .lineNumberOfMasterOffset(start));
        final int hiddenStartLine = hiddenLine + 1;
        final int hiddenStartOffset = textPositionCalculator.offsetOfProjectedLineNumber(hiddenStartLine);
        final int end = textPositionCalculator.masterOffsetToProjectedOffset(start + length);
        final int hiddenLength = end - hiddenStartOffset;

        final StyleRange[] styleRangesToBeRemoved = viewer.getTextWidget().getStyleRanges(hiddenStartOffset,
                hiddenLength);

        for (final StyleRange styleRange : styleRangesToBeRemoved) {
            viewer.getTextWidget().replaceStyleRanges(styleRange.start, styleRange.length, new StyleRange[0]);
        }

        final StyleRange[] styleRangesToBeMoved = viewer.getTextWidget().getStyleRanges(end,
                viewer.getTextWidget().getCharCount() - end);
        for (final StyleRange styleRange : styleRangesToBeMoved) {
            styleRange.start -= hiddenLength;
        }
    }


    protected void rerunHighlightsInRange(final int start, final int length) {
        // need to use master document, not text widget
        for (final Integer offset : highlights.keySet()) {
            if (offset.intValue() >= start && offset.intValue() <= start + length) {
                final DocumentHighlight highlight = highlights.get(offset);

                // if the highlight is still hidden, its projected offset will
                // be -1
                final int projectedOffset = textPositionCalculator.masterOffsetToProjectedOffset(highlight.getOffset());
                if (projectedOffset >= 0) {
                    final StyleRange newStyleRange = documentHighlightToStyleRangeTransformer().from(highlight);
                    newStyleRange.start = projectedOffset;

                    // TODO - are we always out by 1?
                    newStyleRange.length = Math.min(newStyleRange.length, viewer.getTextWidget().getCharCount()
                            - newStyleRange.start);
                    viewer.getTextWidget().setStyleRange(newStyleRange);
                }
            }
        }
    }


    @Override
    protected Transformer<DocumentHighlight, StyleRange> initDocumentHighlightToStyleRangeTransformer(
            final ColourManager colourManager) {
        return new ProjectedDocumentHighlightToStyleRangeTransformer(textPositionCalculator,
                super.initDocumentHighlightToStyleRangeTransformer(colourManager));
    }


    @Override
    protected void updateIconAt(final int offset, final HighlightEvent highlightEvent) {
        final int lineNumber = textPositionCalculator.lineNumberOfMasterOffset(offset);
        // we don't update line 0 - the feature line. As such, the offset/image
        // list starts at line 1, so subtract 1 from this accordingly
        if (lineNumber > 0) {
            updateIconAtLine(lineNumber - 1, highlightEvent);
        }

        // we know the offset is for the text, and the 1st part of any line is
        // the icon character. Therefore, subtract 1 from the offset to
        // get the offset for the start of the line
        final int lineOffset = offset - 1;

        if (HighlightEvent.TestPassed.equals(highlightEvent) && lineOffset > 0) {
            final Annotation annotation = findAnnotationAt(lineOffset);

            if (annotation != null) {
                annotationModel.collapse(annotation);
            }
        }
    }


    private Annotation findAnnotationAt(final int offset) {
        final Integer offsetInteger = Integer.valueOf(offset);
        return oldAnnotationsByMasterOffset.containsKey(offsetInteger) ? oldAnnotationsByMasterOffset
                .get(offsetInteger) : null;
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

        for (final Annotation oldAnnotation : oldAnnotationsByMasterOffset.values()) {
            annotationModel.removeAnnotation(oldAnnotation);
        }
        oldAnnotationsByMasterOffset.clear();

        for (final Annotation newAnnotation : newAnnotations.keySet()) {
            final Position position = newAnnotations.get(newAnnotation);
            annotationModel.addAnnotation(newAnnotation, position);
            oldAnnotationsByMasterOffset.put(Integer.valueOf(position.getOffset()), newAnnotation);
        }
    }


    @Override
    protected HierarchicalIconContainer createIconContainer(final HierarchicalIconContainer parentContainer,
            final IconHighlight icon) {
        return new ProjectedHierarchicalIconContainer(true, icon, parentContainer, masterToProjectedOffsetTransformer);
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

        textPositionCalculator = new ProjectionViewerTextPositionCalculator(viewer);

        annotationModel = viewer.getProjectionAnnotationModel();

        final TextRegionVisibilityToggle visibilityToggle = new CompositeVisiblityToggle(
                new UpdateRenderedTextVisibilityToggle(), //
                new UpdateStyleRangesVisibilityToggle() //
        );
        annotationModelListener = new ToggleTextVisibilityOnCodeFoldingListener(visibilityToggle);

        annotationModel.addAnnotationModelListener(annotationModelListener);
    }


    private void resetFoldedDocument() {
        annotationModel.removeAnnotationModelListener(annotationModelListener);
        final Document document = new Document();
        viewer.setDocument(document, new AnnotationModel());

        annotationModel = viewer.getProjectionAnnotationModel();
        annotationModel.addAnnotationModelListener(annotationModelListener);
        FeatureRunnerPlugin.log(IStatus.INFO, "New Annotation model has "
                + (annotationModel.getAnnotationIterator().hasNext() ? " Some" : " No ") + " Annotations");
    }

    private final class UpdateRenderedTextVisibilityToggle implements TextRegionVisibilityToggle {

        @Override
        public void textVisible(final int offset, final int length) {
            final int end = offset + length;

            if (offset == 0) {
                // We're expanding the top level element, we have to handle this
                // one differently
                doRootNodeExpansion(length);
            } else {
                doSubNodeExpansion(offset, end);
            }
        }


        private void doRootNodeExpansion(final int length) {
            // TODO Auto-generated method stub
            doIconOperation(0, length, new ExpandTextCallback());
        }


        private void doSubNodeExpansion(final int offset, final int end) {
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
