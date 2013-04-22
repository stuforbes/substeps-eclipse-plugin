package com.technophobia.substeps.ui.event;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;

import com.technophobia.eclipse.transformer.Callback1;

public class ClickOnStyledTextLineMouseListener implements MouseListener {

    private final Callback1<String> onLineClickCallback;


    public ClickOnStyledTextLineMouseListener(final Callback1<String> onLineClickCallback) {
        this.onLineClickCallback = onLineClickCallback;
    }


    @Override
    public void mouseDown(final MouseEvent event) {
        final StyledText styledText = (StyledText) event.getSource();
        final int offset = offsetAtPoint(styledText, event.x, event.y);
        if (offset >= 0) {
            final String line = styledText.getLine(styledText.getLineAtOffset(offset));
            if (line != null && line.length() > 0) {
                // 1st character is the icon character
                onLineClickCallback.callback(line.substring(1).trim());
            }
        }
    }


    @Override
    public void mouseDoubleClick(final MouseEvent e) {
        // No-op
    }


    @Override
    public void mouseUp(final MouseEvent e) {
        // No-op
    }


    private int offsetAtPoint(final StyledText styledText, final int x, final int y) {
        try {
            return styledText.getOffsetAtLocation(new Point(x, y));
        } catch (final IllegalArgumentException ex) {
            // point clicked doesn't have an offset, just return -1
            return -1;
        }
    }
}
