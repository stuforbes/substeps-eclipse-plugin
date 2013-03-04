package com.technophobia.substeps.ui.component.position;

import java.util.List;

import org.eclipse.jface.text.Position;

import com.technophobia.substeps.ui.component.TextModelFragment;

public class TextModelFragmentPositionCalculator implements ElementPositionCalculator {

    private final List<TextModelFragment> fragments;


    public TextModelFragmentPositionCalculator(final List<TextModelFragment> fragments) {
        this.fragments = fragments;
    }


    @Override
    public Position positionForOffset(final int offset) {
        // TODO Auto-generated method stub
        return null;
    }

}
