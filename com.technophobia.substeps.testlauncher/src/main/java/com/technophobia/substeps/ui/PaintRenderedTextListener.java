package com.technophobia.substeps.ui;

import java.util.List;

import org.eclipse.swt.custom.PaintObjectEvent;
import org.eclipse.swt.custom.PaintObjectListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Scrollable;

import com.technophobia.substeps.junit.ui.SubstepsIconProvider;
import com.technophobia.substeps.supplier.Supplier;

public class PaintRenderedTextListener implements PaintListener, PaintObjectListener {

    private static final int IMAGE_HEIGHT = 10;
    private static final int IMAGE_WIDTH = 10;

    private final Supplier<List<RenderedText>> renderedTextSupplier;
    private final SubstepsIconProvider iconProvider;
    private final Scrollable component;


    public PaintRenderedTextListener(final Scrollable component, final SubstepsIconProvider iconProvider,
            final Supplier<List<RenderedText>> renderedTextSupplier) {
        this.component = component;
        this.iconProvider = iconProvider;
        this.renderedTextSupplier = renderedTextSupplier;
    }


    @Override
    public void paintControl(final PaintEvent event) {
        final GC gc = event.gc;

        final List<RenderedText> renderedTexts = renderedTextSupplier.get();

        for (final RenderedText renderedText : renderedTexts) {
            if (renderedText.isRendered()) {
                final Point locationAtOffset = renderedText.getLocation();
                if (isInBounds(locationAtOffset)) {

                    final Image image = iconProvider.imageFor(renderedText.getIcon());
                    gc.drawImage(image, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, locationAtOffset.x, locationAtOffset.y + 2,
                            IMAGE_WIDTH, IMAGE_HEIGHT);
                }
            }
        }
    }


    @Override
    public void paintObject(final PaintObjectEvent event) {
        final GC gc = event.gc;
        final StyleRange style = event.style;
        final int start = style.start;

        final List<RenderedText> renderedTexts = renderedTextSupplier.get();

        for (final RenderedText renderedText : renderedTexts) {
            final int offset = renderedText.getOffset();
            if (start == offset && renderedText.isRendered()) {
                final Image image = iconProvider.imageFor(renderedText.getIcon());
                final int x = event.x;
                final int y = event.y + event.ascent - style.metrics.ascent;
                gc.drawImage(image, x, y);
            }
        }
    }


    private boolean isInBounds(final Point locationAtOffset) {
        final Rectangle bounds = component.getClientArea();
        final boolean isWithinX = isWithin(locationAtOffset.x, bounds.x, bounds.width);
        final boolean isWithinY = isWithin(locationAtOffset.y, bounds.y, bounds.height);

        return isWithinX && isWithinY;
    }


    private boolean isWithin(final int objectOffset, final int canvasOffset, final int canvasSize) {
        return objectOffset >= canvasOffset && objectOffset < (canvasOffset + canvasSize);
    }

}
