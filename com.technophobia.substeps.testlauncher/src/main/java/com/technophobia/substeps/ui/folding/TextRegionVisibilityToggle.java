package com.technophobia.substeps.ui.folding;

public interface TextRegionVisibilityToggle {

    void textHidden(int offset, int length);


    void textVisible(int offset, int length);
}
