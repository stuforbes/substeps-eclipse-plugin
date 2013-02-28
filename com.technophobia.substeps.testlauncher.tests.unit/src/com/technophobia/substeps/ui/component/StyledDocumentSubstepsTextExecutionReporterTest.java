package com.technophobia.substeps.ui.component;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.RGB;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.technophobia.substeps.ui.component.StyledDocumentUpdater.HighlightEvent;
import com.technophobia.substeps.ui.model.DocumentHighlight;
import com.technophobia.substeps.ui.model.StyledDocument;
import com.technophobia.substeps.ui.session.SubstepsTestExecutionReporter;

@RunWith(JMock.class)
public class StyledDocumentSubstepsTextExecutionReporterTest {

    private static final RGB GREY = new RGB(128, 128, 128);

    private Mockery context;

    private StyledDocumentUpdater textModelCallback;

    private SubstepsTestExecutionReporter executionReporter;


    @Before
    public void initialise() {
        this.context = new Mockery();

        this.textModelCallback = context.mock(StyledDocumentUpdater.class);

        this.executionReporter = new StyledDocumentSubstepsTextExecutionReporter(textModelCallback);
    }


    @Test
    public void noExecutionNodesProducesEmptyTextModelString() {

        final String text = "";
        final List<DocumentHighlight> highlights = new ArrayList<DocumentHighlight>();

        context.checking(new Expectations() {
            {
                oneOf(textModelCallback).documentChanged(new StyledDocument(text, highlights));
            }
        });

        executionReporter.allExecutionNodesAdded();
    }


    @Test
    public void singleExecutionNodeProducesCorrectTextModelString() {

        final String text = "Feature: This is a feature";
        final List<DocumentHighlight> highlights = new ArrayList<DocumentHighlight>();
        highlights.add(new DocumentHighlight(0, 26, GREY));

        context.checking(new Expectations() {
            {
                oneOf(textModelCallback).documentChanged(new StyledDocument(text, highlights));
            }
        });

        executionReporter.addExecutionNode("1", null, "Feature: This is a feature");
        executionReporter.allExecutionNodesAdded();
    }


    @Test
    public void multipleExecutionNodesProduceCorrectTextModelString() {

        final String text = "Feature: This is a feature\n\tScenario: A scenario\n\t\tGiven something\n\t\tThen something else";
        final List<DocumentHighlight> highlights = new ArrayList<DocumentHighlight>();
        highlights.add(new DocumentHighlight(0, 26, GREY));
        highlights.add(new DocumentHighlight(27, 21, GREY));
        highlights.add(new DocumentHighlight(49, 17, GREY));
        highlights.add(new DocumentHighlight(67, 21, GREY));

        context.checking(new Expectations() {
            {
                oneOf(textModelCallback).documentChanged(new StyledDocument(text, highlights));
            }
        });

        executionReporter.addExecutionNode("1", null, "Feature: This is a feature");
        executionReporter.addExecutionNode("2", "1", "Scenario: A scenario");
        executionReporter.addExecutionNode("3", "2", "Given something");
        executionReporter.addExecutionNode("4", "2", "Then something else");
        executionReporter.allExecutionNodesAdded();
    }


    @Test
    public void nestedExecutionNodesProduceCorrectTextModelString() {

        final String text = "Feature: This is a feature\n\tScenario: A scenario\n\t\tGiven something\n\t\t\tGiven some substep\n\t\tThen something else";
        final List<DocumentHighlight> highlights = new ArrayList<DocumentHighlight>();
        highlights.add(new DocumentHighlight(0, 26, GREY));
        highlights.add(new DocumentHighlight(27, 21, GREY));
        highlights.add(new DocumentHighlight(49, 17, GREY));
        highlights.add(new DocumentHighlight(67, 21, GREY));
        highlights.add(new DocumentHighlight(89, 21, GREY));

        context.checking(new Expectations() {
            {
                oneOf(textModelCallback).documentChanged(new StyledDocument(text, highlights));
            }
        });

        executionReporter.addExecutionNode("1", null, "Feature: This is a feature");
        executionReporter.addExecutionNode("2", "1", "Scenario: A scenario");
        executionReporter.addExecutionNode("3", "2", "Given something");
        executionReporter.addExecutionNode("3", "3", "Given some substep");
        executionReporter.addExecutionNode("4", "2", "Then something else");
        executionReporter.allExecutionNodesAdded();
    }


    @Test
    public void resetExecutionStateRemovesAllTextFragments() {

        final String text = "";
        final List<DocumentHighlight> highlights = new ArrayList<DocumentHighlight>();

        context.checking(new Expectations() {
            {
                oneOf(textModelCallback).documentChanged(new StyledDocument(text, highlights));
            }
        });

        executionReporter.addExecutionNode("1", null, "Feature: This is a feature");
        executionReporter.addExecutionNode("2", "1", "Scenario: A scenario");
        executionReporter.addExecutionNode("3", "2", "Given something");
        executionReporter.addExecutionNode("4", "2", "Then something else");
        executionReporter.resetExecutionState();
        executionReporter.allExecutionNodesAdded();
    }


    @Test
    public void executingNodeUpdatesCorrectHighlight() {

        context.checking(new Expectations() {
            {
                oneOf(textModelCallback).highlightChanged(HighlightEvent.TestPassed,
                        new DocumentHighlight(30, 20, true, new RGB(0, 0, 0)));
            }
        });

        executionReporter.addExecutionNode("1", null, "textfor1.");
        executionReporter.addExecutionNode("2", "1", "textfor2");
        executionReporter.addExecutionNode("3", "1", "textfor3");
        executionReporter.addExecutionNode("4", "1", "textfor4thats20char");
        executionReporter.executingNode("4");
    }
}
