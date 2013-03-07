package com.technophobia.substeps.ui;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.custom.PaintObjectEvent;
import org.eclipse.swt.custom.PaintObjectListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import com.technophobia.substeps.FeatureRunnerPlugin;
import com.technophobia.substeps.colour.ColourManager;
import com.technophobia.substeps.junit.ui.SubstepsIconProvider;
import com.technophobia.substeps.supplier.Supplier;

public class PaintRenderedTextListener implements PaintListener, PaintObjectListener {

    private static final int IMAGE_HEIGHT = 10;
    private static final int IMAGE_WIDTH = 10;

    private final Supplier<List<RenderedText>> renderedTextSupplier;
    private final SubstepsIconProvider iconProvider;
    private final StyledText component;
    private final ColourManager colourManager;


    public PaintRenderedTextListener(final StyledText component, final SubstepsIconProvider iconProvider,
            final Supplier<List<RenderedText>> renderedTextSupplier, final ColourManager colourManager) {
        this.component = component;
        this.iconProvider = iconProvider;
        this.renderedTextSupplier = renderedTextSupplier;
        this.colourManager = colourManager;
    }


    @Override
    public void paintControl(final PaintEvent event) {
        final GC gc = event.gc;
        final List<RenderedText> renderedTexts = renderedTextSupplier.get();

        if (!renderedTexts.isEmpty()) {
            for (final RenderedText renderedText : renderedTexts) {
                if (renderedText.isRendered()) {
                    final StyleRange styleRange = component.getStyleRangeAtOffset(renderedText.getOffset());
                    if (styleRange != null) {
                        final Point locationAtOffset = renderedText.getLocation();
                        final Image image = iconProvider.imageFor(renderedText.getIcon());
                        gc.drawImage(image, locationAtOffset.x, locationAtOffset.y);
                    }

                    // if (renderedText.getOffset() == 13) {
                    // // FeatureRunnerPlugin.log(IStatus.INFO,
                    // // "Scrollbounds: " + scrollBounds + ", clipping "
                    // // + clipping + ", Offset: " + locationAtOffset.y +
                    // // ", lower bound: " + scrollBounds.y
                    // // + ", upper bound: " + (scrollBounds.y +
                    // // scrollBounds.height));
                    // }
                    // if (isInBounds(locationAtOffset, scrollBounds)) {
                    //
                    // final Image image =
                    // iconProvider.imageFor(renderedText.getIcon());
                    // // gc.drawImage(image, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT,
                    // // locationAtOffset.x,
                    // // (locationAtOffset.y + 2), IMAGE_WIDTH, IMAGE_HEIGHT);
                    // gc.setForeground(colourManager.getColor(new RGB(0, 0,
                    // 0)));
                    // gc.drawLine(locationAtOffset.x, locationAtOffset.y, 200,
                    // locationAtOffset.y);
                    // FeatureRunnerPlugin.log(IStatus.INFO,
                    // "Drawing line at " + locationAtOffset + ", " +
                    // gc.getForeground());
                    //
                    // // if (renderedText.getOffset() == 13) {
                    // // FeatureRunnerPlugin.log(IStatus.INFO,
                    // // "Rendering top icon at (" + (locationAtOffset.x)
                    // // + ", " + ((locationAtOffset.y + 2)) + ")");
                    // // }
                    // }
                }
            }
        }
    }


    @Override
    public void paintObject(final PaintObjectEvent event) {
        final GC gc = event.gc;
        final StyleRange style = event.style;
        final int start = style.start;

        FeatureRunnerPlugin.log(IStatus.INFO, "Attempting to render style range at offset" + start);

        final List<RenderedText> renderedTexts = renderedTextSupplier.get();

        for (final RenderedText renderedText : renderedTexts) {
            final int offset = renderedText.getOffset();
            if (start == offset && renderedText.isRendered()) {
                final Image image = iconProvider.imageFor(renderedText.getIcon());
                final int x = event.x;
                final int y = event.y + event.ascent - style.metrics.ascent;
                FeatureRunnerPlugin.log(
                        IStatus.INFO,
                        "Drawing image for icon " + renderedText + " - "
                                + component.getLine(component.getLineAtOffset(offset)));
                gc.drawImage(image, x, y);
                break;
            }
        }
    }


    private boolean isInBounds(final Point locationAtOffset, final Rectangle bounds) {
        final boolean isWithinX = isWithin(locationAtOffset.x, bounds.x, bounds.width);
        final boolean isWithinY = isWithin(locationAtOffset.y, bounds.y, bounds.height);

        return isWithinX && isWithinY;
    }


    private boolean isWithin(final int objectOffset, final int canvasOffset, final int canvasSize) {
        return objectOffset >= canvasOffset && objectOffset <= (canvasOffset + canvasSize);
    }

}
