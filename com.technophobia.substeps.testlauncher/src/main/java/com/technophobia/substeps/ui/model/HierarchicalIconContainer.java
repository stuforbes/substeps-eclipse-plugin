package com.technophobia.substeps.ui.model;

import com.technophobia.substeps.ui.component.SubstepsIcon;
import com.technophobia.substeps.ui.model.IconHighlight;

public class HierarchicalIconContainer {

    private boolean expanded;

    private final HierarchicalIconContainer parent;
    private final IconHighlight iconHighlight;


    public HierarchicalIconContainer(final boolean isExpanded, final IconHighlight iconHighlight,
            final HierarchicalIconContainer parent) {
        this.expanded = isExpanded;
        this.iconHighlight = iconHighlight;
        this.parent = parent;
    }


    public boolean isRendered() {
        return parent == null || parent.canShowChildren();
    }


    public int getOffset() {
        return iconHighlight.getOffset();
    }


    public SubstepsIcon getIcon() {
        return iconHighlight.getIcon();
    }


    public void mutateIconTo(final SubstepsIcon icon) {
        iconHighlight.mutateIconTo(icon);
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


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (expanded ? 1231 : 1237);
        result = prime * result + ((iconHighlight == null) ? 0 : iconHighlight.hashCode());
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
        final HierarchicalIconContainer other = (HierarchicalIconContainer) obj;
        if (expanded != other.expanded)
            return false;
        if (iconHighlight == null) {
            if (other.iconHighlight != null)
                return false;
        } else if (!iconHighlight.equals(other.iconHighlight))
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
        return "Icon " + iconHighlight + " - expanded - " + expanded;
    }
}
