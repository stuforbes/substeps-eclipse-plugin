package com.technophobia.substeps.ui.paint;

import java.util.List;

import org.eclipse.swt.custom.PaintObjectEvent;
import org.eclipse.swt.custom.PaintObjectListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

import com.technophobia.substeps.junit.ui.SubstepsIconProvider;
import com.technophobia.substeps.supplier.Supplier;
import com.technophobia.substeps.ui.model.HierarchicalIconContainer;

public class PaintRenderedTextListener implements PaintObjectListener {

    private final Supplier<List<HierarchicalIconContainer>> iconSupplier;
    private final SubstepsIconProvider iconProvider;


    public PaintRenderedTextListener(final SubstepsIconProvider iconProvider,
            final Supplier<List<HierarchicalIconContainer>> supplier) {
        this.iconProvider = iconProvider;
        this.iconSupplier = supplier;
    }


    @Override
    public void paintObject(final PaintObjectEvent event) {
        final GC gc = event.gc;
        final StyleRange style = event.style;
        final int start = style.start;

        final List<HierarchicalIconContainer> icons = iconSupplier.get();

        for (final HierarchicalIconContainer icon : icons) {
            final int offset = icon.getOffset();
            if (start == offset && icon.isRendered()) {
                final Image image = iconProvider.imageFor(icon.getIcon());
                final int x = event.x;
                final int y = event.y + event.ascent - style.metrics.ascent;
                gc.drawImage(image, x, y);
                break;
            }
        }
    }

}
