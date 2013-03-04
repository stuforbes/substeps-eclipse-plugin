package com.technophobia.substeps.ui.component;


public interface HierarchicalTextStructureFactory {

    HierarchicalTextStructure createTextStructureFor(int currentPosition, int currentLine, String id, String parentId,
            String text);
}
