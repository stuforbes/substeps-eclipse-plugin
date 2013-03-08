package com.technophobia.substeps.ui.model;

import org.eclipse.swt.graphics.RGB;

public class DocumentHighlight {

    private final int offset;
    private final int length;
    private final RGB colour;

    private final boolean bold;


    public DocumentHighlight(final int offset, final int length, final RGB colour) {
        this(offset, length, false, colour);
    }


    public DocumentHighlight(final int offset, final int length, final boolean bold, final RGB colour) {
        this.offset = offset;
        this.length = length;
        this.bold = bold;
        this.colour = colour;
    }


    public int getOffset() {
        return offset;
    }


    public int getLength() {
        return length;
    }


    public boolean isBold() {
        return bold;
    }


    public RGB getColour() {
        return colour;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (bold ? 1231 : 1237);
        result = prime * result + ((colour == null) ? 0 : colour.hashCode());
        result = prime * result + length;
        result = prime * result + offset;
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
        final DocumentHighlight other = (DocumentHighlight) obj;
        if (bold != other.bold)
            return false;
        if (colour == null) {
            if (other.colour != null)
                return false;
        } else if (!colour.equals(other.colour))
            return false;
        if (length != other.length)
            return false;
        if (offset != other.offset)
            return false;
        return true;
    }


    @Override
    public String toString() {
        return "Text at offset " + offset + ", length " + length + ", bold=" + bold + ", colour " + colour;
    }
}
