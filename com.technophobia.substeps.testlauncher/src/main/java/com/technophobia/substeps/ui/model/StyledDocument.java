package com.technophobia.substeps.ui.model;

import java.util.List;

import org.eclipse.jface.text.Position;

public class StyledDocument {

    private final String text;
    private final List<DocumentHighlight> highlights;
    private final List<Position> positions;


    public StyledDocument(final String text, final List<DocumentHighlight> highlights, final List<Position> positions) {
        this.text = text;
        this.highlights = highlights;
        this.positions = positions;
    }


    public String getText() {
        return text;
    }


    public List<DocumentHighlight> getHighlights() {
        return highlights;
    }


    public List<Position> getPositions() {
        return positions;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((highlights == null) ? 0 : highlights.hashCode());
        result = prime * result + ((positions == null) ? 0 : positions.hashCode());
        result = prime * result + ((text == null) ? 0 : text.hashCode());
        return result;
    }


    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final StyledDocument other = (StyledDocument) obj;
        if (highlights == null) {
            if (other.highlights != null)
                return false;
        } else if (!highlights.equals(other.highlights))
            return false;
        if (positions == null) {
            if (other.positions != null)
                return false;
        } else if (!positions.equals(other.positions))
            return false;
        if (text == null) {
            if (other.text != null)
                return false;
        } else if (!text.equals(other.text))
            return false;
        return true;
    }


    @Override
    public String toString() {
        return text + " with highlights " + highlights + "; and positions " + positions;
    }
}
