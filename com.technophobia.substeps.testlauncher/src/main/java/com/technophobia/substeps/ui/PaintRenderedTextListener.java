package com.technophobia.substeps.ui;

import java.util.List;

import org.eclipse.swt.custom.PaintObjectEvent;
import org.eclipse.swt.custom.PaintObjectListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import com.technophobia.substeps.junit.ui.SubstepsIconProvider;
import com.technophobia.substeps.supplier.Supplier;

public class PaintRenderedTextListener implements PaintObjectListener {

    private final Supplier<List<RenderedText>> renderedTextSupplier;
    private final SubstepsIconProvider iconProvider;


    public PaintRenderedTextListener(final SubstepsIconProvider iconProvider,
            final Supplier<List<RenderedText>> renderedTextSupplier) {
        this.iconProvider = iconProvider;
        this.renderedTextSupplier = renderedTextSupplier;
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
                // FeatureRunnerPlugin.log(
                // IStatus.INFO,
                // "Drawing image for icon " + renderedText + " - "
                // + component.getLine(component.getLineAtOffset(offset)));
                gc.drawImage(image, x, y);
                break;
            }
        }
    }


    private int offsetOf(final RenderedText renderedText) {
        // Not particularly nice here - StyleRanges are not aware of code
        // folding, so projected offsets don't work.
        // If the RenderedText here is a ProjectedRenderedText, we need the
        // master offset
        if (renderedText instanceof ProjectedRenderedText) {
            return ((ProjectedRenderedText) renderedText).getMasterOffset();
        }
        return renderedText.getOffset();
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
