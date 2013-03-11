package com.technophobia.substeps.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.BadLocationException;

import com.technophobia.substeps.FeatureRunnerPlugin;

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


    public int lineNumberOfMasterOffset(final int offset) {
        try {
            return viewer.getDocument().getLineOfOffset(offset);
        } catch (final BadLocationException ex) {
            FeatureRunnerPlugin.log(IStatus.WARNING, "Could not get line number for offset " + offset
                    + ", returning -1");
            return -1;
        }
    }


    public int offsetOfMasterLineNumber(final int lineNumber) {
        try {
            return viewer.getDocument().getLineOffset(lineNumber);
        } catch (final BadLocationException e) {
            FeatureRunnerPlugin.log(IStatus.WARNING, "Could not get offset for line number" + lineNumber
                    + ", returning -1");
            return -1;
        }
    }
}
