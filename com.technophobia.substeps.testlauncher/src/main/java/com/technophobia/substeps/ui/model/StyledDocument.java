package com.technophobia.substeps.ui.model;

import java.util.List;

public class StyledDocument {

    private final String text;
    private final List<DocumentHighlight> highlights;


    public StyledDocument(final String text, final List<DocumentHighlight> highlights) {
        this.text = text;
        this.highlights = highlights;
    }


    public String getText() {
        return text;
    }


    public List<DocumentHighlight> getHighlights() {
        return highlights;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((highlights == null) ? 0 : highlights.hashCode());
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
        if (text == null) {
            if (other.text != null)
                return false;
        } else if (!text.equals(other.text))
            return false;
        return true;
    }


    @Override
    public String toString() {
        return text + " with highlights " + highlights;
    }
}
