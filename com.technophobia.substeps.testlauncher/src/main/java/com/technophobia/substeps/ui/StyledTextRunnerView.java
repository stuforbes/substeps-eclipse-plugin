package com.technophobia.substeps.ui;

import java.util.ArrayList;
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
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.OverviewRuler;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.PaintObjectEvent;
import org.eclipse.swt.custom.PaintObjectListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;

import com.technophobia.substeps.FeatureRunnerPlugin;
import com.technophobia.substeps.colour.ColourManager;
import com.technophobia.substeps.junit.ui.SubstepsIconProvider;
import com.technophobia.substeps.ui.component.ListDelegateHierarchicalTextCollection;
import com.technophobia.substeps.ui.component.StyledDocumentSubstepsTextExecutionReporter;
import com.technophobia.substeps.ui.component.StyledDocumentUpdater;
import com.technophobia.substeps.ui.component.StyledDocumentUpdater.HighlightEvent;
import com.technophobia.substeps.ui.component.SubstepsIcon;
import com.technophobia.substeps.ui.component.TextModelFragmentFactory;
import com.technophobia.substeps.ui.model.DocumentHighlight;
import com.technophobia.substeps.ui.model.StyledDocument;
import com.technophobia.substeps.ui.session.SubstepsTestExecutionReporter;

public class StyledTextRunnerView implements RunnerView {

    private static final RGB WHITE = new RGB(255, 255, 255);
    private static final RGB GREY = new RGB(128, 128, 128);
    private static final int IMAGE_HEIGHT = 10;
    private static final int IMAGE_WIDTH = 10;

    private StyledText textComponent;
    private final ColourManager colourManager;

    private final SubstepsIconProvider iconProvider;
    private ProjectionViewer viewer;

    private List<Integer> offsets;
    private List<SubstepsIcon> images;

    private Annotation[] oldAnnotations;
    private ProjectionAnnotationModel annotationModel;

    private final StyledDocumentUpdater styledDocumentUpdater;


    public StyledTextRunnerView(final ColourManager colourManager, final SubstepsIconProvider iconProvider) {
        this.colourManager = colourManager;
        this.iconProvider = iconProvider;

        this.offsets = new ArrayList<Integer>();
        this.images = new ArrayList<SubstepsIcon>();

        this.styledDocumentUpdater = updateTextComponentCallback();
    }


    @Override
    public void createPartControl(final Composite parent) {
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

        textComponent = viewer.getTextWidget();

        // textComponent.setAlwaysShowScrollBars(true);
        final Font font = new Font(parent.getDisplay(), parent.getFont().getFontData()[0].name, 10, SWT.NORMAL);
        textComponent.setFont(font);
        textComponent.setLineSpacing(5);
        textComponent.setEditable(false);

        textComponent.addPaintObjectListener(new PaintObjectListener() {

            @Override
            public void paintObject(final PaintObjectEvent event) {
                final GC gc = event.gc;
                final StyleRange style = event.style;
                final int start = style.start;
                for (int i = 0; i < offsets.size(); i++) {
                    final int offset = offsets.get(i);
                    if (start == offset) {
                        final Image image = iconProvider.imageFor(images.get(i));
                        final int x = event.x;
                        final int y = event.y + event.ascent - style.metrics.ascent;
                        gc.drawImage(image, x, y);
                    }
                }
            }
        });
    }


    public void updateFoldingStructure(final List<Position> positions) {
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


    @Override
    public void dispose() {
        textComponent.dispose();
        textComponent = null;

        images.clear();
        offsets.clear();
    }


    @Override
    public SubstepsTestExecutionReporter executionReporter() {
        final ListDelegateHierarchicalTextCollection textCollection = new ListDelegateHierarchicalTextCollection();
        final TextChangedToDocumentUpdater stateChangeHighlighter = new TextChangedToDocumentUpdater(
                styledDocumentUpdater);
        final TextModelFragmentFactory textModelFragmentFactory = new TextModelFragmentFactory(textCollection,
                stateChangeHighlighter);
        return new StyledDocumentSubstepsTextExecutionReporter(textCollection, textModelFragmentFactory,
                stateChangeHighlighter);
    }


    protected void resetTextTo(final StyledDocument styledDocument) {
        textComponent.setStyleRange(null);
        viewer.getDocument().set(styledDocument.getText());

        final int lineCount = textComponent.getLineCount();
        images = new ArrayList<SubstepsIcon>(lineCount);
        offsets = new ArrayList<Integer>(lineCount);

        prepareTextStyleRanges(lineCount);
        updateFoldingStructure(styledDocument.getPositions());
    }


    private void prepareTextStyleRanges(final int lineCount) {
        for (int i = 1; i < lineCount; i++) {
            final int offset = textComponent.getOffsetAtLine(i);
            createUnprocessedTextStyleRange(i, offset);
            createIconStyleRange(offset);
        }
    }


    private void createUnprocessedTextStyleRange(final int line, final int offset) {
        final int length = textComponent.getLine(line).length() - 1;
        textComponent.setStyleRange(new StyleRange(offset + 1, length, colourManager.getColor(GREY), colourManager
                .getColor(WHITE), SWT.NONE));
    }


    private void createIconStyleRange(final int offset) {
        final StyleRange style = new StyleRange();
        textComponent.replaceTextRange(offset, 1, "\uFFFC");
        style.start = offset;
        style.length = 1;
        style.metrics = new GlyphMetrics(IMAGE_HEIGHT, 0, IMAGE_WIDTH);
        textComponent.setStyleRange(style);
        offsets.add(offset);
        images.add(SubstepsIcon.SubstepNoResult);
    }


    protected void addHighlight(final DocumentHighlight highlight) {
        // StyleRange offset is line number offset + 1 - this is because the
        // result icon is at position 0
        textComponent.setStyleRange(new StyleRange(textComponent.getOffsetAtLine(highlight.getLine()) + 1, highlight
                .getLength(), colourManager.getColor(highlight.getColour()), colourManager.getColor(WHITE), highlight
                .isBold() ? SWT.BOLD : SWT.NONE));
    }


    protected void updateIconAt(final int line, final HighlightEvent highlightEvent) {

        // we don't update line 0 - the feature line. As such, the offset/image
        // list starts at line 1, so subtract 1 from this accordingly
        if (line > 0) {
            final int normalizedLine = line - 1;

            if (HighlightEvent.TestPassed.equals(highlightEvent)) {
                images.remove(normalizedLine);
                images.add(normalizedLine, SubstepsIcon.SubstepPassed);
            } else if (HighlightEvent.TestFailed.equals(highlightEvent)) {
                images.remove(normalizedLine);
                images.add(normalizedLine, SubstepsIcon.SubstepFailed);
            } else if (HighlightEvent.NoChange.equals(highlightEvent)) {
                // No change
            } else {
                FeatureRunnerPlugin.log(IStatus.WARNING, "Unexpected highlight event type");
            }
        }
    }


    private StyledDocumentUpdater updateTextComponentCallback() {
        return new StyledDocumentUpdater() {

            @Override
            public void highlightChanged(final HighlightEvent highlightEvent, final DocumentHighlight highlight) {
                textComponent.getDisplay().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        addHighlight(highlight);

                        updateIconAt(highlight.getLine(), highlightEvent);
                    }
                });
            }


            @Override
            public void documentChanged(final StyledDocument document) {
                textComponent.getDisplay().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        resetTextTo(document);
                    }
                });
            }
        };
    }
}
