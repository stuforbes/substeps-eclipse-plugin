package com.technophobia.substeps.ui.component;

public enum SubstepsIcon implements com.technophobia.substeps.junit.ui.SubstepsIcon {
    SubstepPassed("eview16/tick.gif"), //
    SubstepFailed("eview16/cross.gif"), //
    SubstepNoResult("eview16/no_result.gif"), //

    ActionShowErrors("eview16/substeps.gif");

    private final String path;


    private SubstepsIcon(final String path) {
        this.path = path;
    }


    @Override
    public String getPath() {
        return path;
    }
}
