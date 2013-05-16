package com.technophobia.substeps.ui.folding;

public interface TextPositionCalculator {

    int projectedOffsetToMasterOffset(int projectedOffset);


    int projectedLineToMasterLine(int projectedLine);


    int masterOffsetToProjectedOffset(int masterOffset);


    int masterLineToProjectedLine(int masterLine);


    int lineNumberOfProjectedOffset(int offset);


    int offsetOfProjectedLineNumber(int lineNumber);


    int lineNumberOfMasterOffset(int offset);


    int offsetOfMasterLineNumber(int lineNumber);

}