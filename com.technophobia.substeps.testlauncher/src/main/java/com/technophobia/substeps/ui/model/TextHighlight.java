package com.technophobia.substeps.ui.model;

import org.eclipse.swt.graphics.RGB;

public class TextHighlight extends DocumentHighlight {

    private final RGB colour;
    private final boolean bold;


    public TextHighlight(final int offset, final int length, final RGB colour) {
        this(offset, length, false, colour);
    }


    public TextHighlight(final int offset, final int length, final boolean bold, final RGB colour) {
        super(offset, length);
        this.bold = bold;
        this.colour = colour;
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
        int result = super.hashCode();
        result = prime * result + (bold ? 1231 : 1237);
        result = prime * result + ((colour == null) ? 0 : colour.hashCode());
        return result;
    }


    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        final TextHighlight other = (TextHighlight) obj;
        if (bold != other.bold)
            return false;
        if (colour == null) {
            if (other.colour != null)
                return false;
        } else if (!colour.equals(other.colour))
            return false;
        return true;
    }


    @Override
    public String toString() {
        return super.toString() + ", bold = " + bold + ", colour = " + colour;
    }
}
