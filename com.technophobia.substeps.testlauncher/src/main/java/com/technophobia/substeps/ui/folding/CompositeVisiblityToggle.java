package com.technophobia.substeps.ui.folding;

public class CompositeVisiblityToggle implements TextRegionVisibilityToggle {

    private final TextRegionVisibilityToggle[] delegates;


    public CompositeVisiblityToggle(final TextRegionVisibilityToggle... delegates) {
        this.delegates = delegates;
    }


    @Override
    public void textHidden(final int offset, final int length) {
        for (final TextRegionVisibilityToggle delegate : delegates) {
            delegate.textHidden(offset, length);
        }
    }


    @Override
    public void textVisible(final int offset, final int length) {
        for (final TextRegionVisibilityToggle delegate : delegates) {
            delegate.textVisible(offset, length);
        }
    }

}
