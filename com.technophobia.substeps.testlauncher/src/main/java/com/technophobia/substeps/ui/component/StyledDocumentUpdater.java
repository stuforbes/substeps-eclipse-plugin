package com.technophobia.substeps.ui.component;

import com.technophobia.substeps.ui.model.DocumentHighlight;
import com.technophobia.substeps.ui.model.StyledDocument;

public interface StyledDocumentUpdater {

    public enum HighlightEvent {
        TestPassed, //
        TestFailed, //
        NoChange;
    };


    void documentChanged(StyledDocument styledDocument);


    void highlightChanged(HighlightEvent highlightEvent, DocumentHighlight highlight);


    void tearDown();
}
