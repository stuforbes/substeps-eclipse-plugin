package com.technophobia.substeps.ui.model;

import com.technophobia.substeps.ui.component.SubstepsIcon;

public class IconHighlight extends DocumentHighlight {

    private final int imageWidth;
    private final int imageHeight;
    private SubstepsIcon icon;


    public IconHighlight(final int offset, final int length, final SubstepsIcon icon, final int imageWidth,
            final int imageHeight) {
        super(offset, length);
        this.icon = icon;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }


    public SubstepsIcon getIcon() {
        return icon;
    }


    public void mutateIconTo(final SubstepsIcon icon) {
        this.icon = icon;
    }


    public int getImageWidth() {
        return imageWidth;
    }


    public int getImageHeight() {
        return imageHeight;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + imageHeight;
        result = prime * result + imageWidth;
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
        final IconHighlight other = (IconHighlight) obj;
        if (imageHeight != other.imageHeight)
            return false;
        if (imageWidth != other.imageWidth)
            return false;
        return true;
    }


    @Override
    public String toString() {
        return super.toString() + "icon = " + icon + ", image width = " + imageWidth + ", image height = "
                + imageHeight;
    }
}
