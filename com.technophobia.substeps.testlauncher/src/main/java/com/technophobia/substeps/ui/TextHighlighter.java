package com.technophobia.substeps.ui;

import java.util.List;

import org.eclipse.jface.text.Position;

import com.technophobia.substeps.ui.component.HierarchicalTextStructure;

public interface TextHighlighter {

    void highlight(HierarchicalTextStructure text);


    void documentChanged(String document, Iterable<HierarchicalTextStructure> textStructures, List<Position> positions);


    void reset();
}
