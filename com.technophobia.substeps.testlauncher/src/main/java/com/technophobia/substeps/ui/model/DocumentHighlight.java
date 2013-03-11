package com.technophobia.substeps.ui.model;

public abstract class DocumentHighlight {

    private final int offset;
    private final int length;


    public DocumentHighlight(final int offset, final int length) {
        this.offset = offset;
        this.length = length;
    }


    public int getOffset() {
        return offset;
    }


    public int getLength() {
        return length;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
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
        if (length != other.length)
            return false;
        if (offset != other.offset)
            return false;
        return true;
    }


    @Override
    public String toString() {
        return "Highlight at offset " + offset + ", length " + length;
    }
}
