package com.technophobia.substeps.ui;

public class ProjectedTextPositionCalculator {

    private final MappingEnabledProjectionViewer viewer;


    public ProjectedTextPositionCalculator(final MappingEnabledProjectionViewer viewer) {
        this.viewer = viewer;
    }


    public int projectedOffsetToMasterOffset(final int projectedOffset) {
        return viewer.masterDocumentOffset(projectedOffset);
    }


    public int masterOffsetToProjectedOffset(final int masterOffset) {
        return viewer.projectedOffset(masterOffset);
    }


    public int masterLineToProjectedLine(final int masterLine) {
        return viewer.projectedLineNumber(masterLine);
    }


    public int lineNumberOfProjectedOffset(final int offset) {
        return viewer.getTextWidget().getLineAtOffset(offset);
    }


    public int offsetOfProjectedLineNumber(final int lineNumber) {
        return viewer.getTextWidget().getOffsetAtLine(lineNumber);
    }
}
