package com.technophobia.substeps.ui;

import com.technophobia.substeps.ui.component.SubstepsIcon;

public class RenderedText {

    private boolean expanded;
    private SubstepsIcon icon;

    private final int offset;
    private final RenderedText parent;


    public RenderedText(final boolean isExpanded, final SubstepsIcon icon, final int offset, final RenderedText parent) {
        this.expanded = isExpanded;
        this.icon = icon;
        this.offset = offset;
        this.parent = parent;
    }


    public boolean isRendered() {
        return parent == null || parent.canShowChildren();
    }


    private boolean canShowChildren() {
        return expanded && (parent == null || parent.canShowChildren());
    }


    public void collapse() {
        this.expanded = false;
    }


    public void expand() {
        this.expanded = true;
    }


    public SubstepsIcon getIcon() {
        return icon;
    }


    public void mutateIconTo(final SubstepsIcon newIcon) {
        this.icon = newIcon;
    }


    public int getOffset() {
        return offset;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (expanded ? 1231 : 1237);
        result = prime * result + ((icon == null) ? 0 : icon.hashCode());
        result = prime * result + offset;
        result = prime * result + ((parent == null) ? 0 : parent.hashCode());
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
        final RenderedText other = (RenderedText) obj;
        if (expanded != other.expanded)
            return false;
        if (icon != other.icon)
            return false;
        if (offset != other.offset)
            return false;
        if (parent == null) {
            if (other.parent != null)
                return false;
        } else if (!parent.equals(other.parent))
            return false;
        return true;
    }


    @Override
    public String toString() {
        return "Icon " + icon + " at offset " + offset + " - expanded - " + expanded;
    }
}
