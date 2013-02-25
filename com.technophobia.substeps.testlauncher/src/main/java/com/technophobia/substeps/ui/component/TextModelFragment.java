package com.technophobia.substeps.ui.component;

public class TextModelFragment {

    private final String id;
    private final String text;

    private final int depth;

    private final int startPos;
    private final int length;


    public TextModelFragment(final String id, final String text, final int depth, final int startPos, final int length) {
        this.id = id;
        this.text = text;
        this.depth = depth;
        this.startPos = startPos;
        this.length = length;
    }


    public String id() {
        return id;
    }


    public String text() {
        return text;
    }


    public String indentedText() {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            sb.append("\t");
        }
        sb.append(text);
        return sb.toString();
    }


    public int depth() {
        return depth;
    }


    public int startPosition() {
        return startPos;
    }


    public int length() {
        return length;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + depth;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + length;
        result = prime * result + startPos;
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
        final TextModelFragment other = (TextModelFragment) obj;
        if (depth != other.depth)
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (length != other.length)
            return false;
        if (startPos != other.startPos)
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
        return id + ": " + text + " - " + startPos + ", " + length + ")";
    }
}
