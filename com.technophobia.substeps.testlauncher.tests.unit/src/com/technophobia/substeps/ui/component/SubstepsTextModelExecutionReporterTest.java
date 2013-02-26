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

import com.technophobia.substeps.supplier.Callback1;
import com.technophobia.substeps.ui.model.DocumentHighlight;
import com.technophobia.substeps.ui.model.StyledDocument;
import com.technophobia.substeps.ui.session.SubstepsTestExecutionReporter;

@RunWith(JMock.class)
public class SubstepsTextModelExecutionReporterTest {

    private static final RGB GREY = new RGB(128, 128, 128);

    private Mockery context;

    private Callback1<StyledDocument> textModelCallback;

    private SubstepsTestExecutionReporter executionReporter;


    @SuppressWarnings("unchecked")
    @Before
    public void initialise() {
        this.context = new Mockery();

        this.textModelCallback = context.mock(Callback1.class);

        this.executionReporter = new StyledDocumentSubstepsTextExecutionReporter(textModelCallback);
    }


    @Test
    public void noExecutionNodesProducesEmptyTextModelString() {

        context.checking(new Expectations() {
            {
                oneOf(textModelCallback).doCallback(new StyledDocument("", new ArrayList<DocumentHighlight>()));
            }
        });

        executionReporter.allExecutionNodesAdded();
    }


    @Test
    public void singleExecutionNodeProducesCorrectTextModelString() {

        final List<DocumentHighlight> highlights = new ArrayList<DocumentHighlight>();
        highlights.add(new DocumentHighlight(0, 26, GREY));

        context.checking(new Expectations() {
            {
                oneOf(textModelCallback).doCallback(new StyledDocument("Feature: This is a feature", highlights));
            }
        });

        executionReporter.addExecutionNode("1", null, "Feature: This is a feature");
        executionReporter.allExecutionNodesAdded();
    }


    @Test
    public void multipleExecutionNodesProduceCorrectTextModelString() {
        final List<DocumentHighlight> highlights = new ArrayList<DocumentHighlight>();
        highlights.add(new DocumentHighlight(0, 26, GREY));
        highlights.add(new DocumentHighlight(27, 21, GREY));
        highlights.add(new DocumentHighlight(49, 17, GREY));
        highlights.add(new DocumentHighlight(67, 21, GREY));

        context.checking(new Expectations() {
            {
                oneOf(textModelCallback)
                        .doCallback(
                                new StyledDocument(
                                        "Feature: This is a feature\n\tScenario: A scenario\n\t\tGiven something\n\t\tThen something else",
                                        highlights));
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

        final List<DocumentHighlight> highlights = new ArrayList<DocumentHighlight>();
        highlights.add(new DocumentHighlight(0, 26, GREY));
        highlights.add(new DocumentHighlight(27, 21, GREY));
        highlights.add(new DocumentHighlight(49, 17, GREY));
        highlights.add(new DocumentHighlight(67, 21, GREY));
        highlights.add(new DocumentHighlight(89, 21, GREY));

        context.checking(new Expectations() {
            {
                oneOf(textModelCallback)
                        .doCallback(
                                new StyledDocument(
                                        "Feature: This is a feature\n\tScenario: A scenario\n\t\tGiven something\n\t\t\tGiven some substep\n\t\tThen something else",
                                        highlights));
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

        context.checking(new Expectations() {
            {
                oneOf(textModelCallback).doCallback(new StyledDocument("", new ArrayList<DocumentHighlight>()));
            }
        });

        executionReporter.addExecutionNode("1", null, "Feature: This is a feature");
        executionReporter.addExecutionNode("2", "1", "Scenario: A scenario");
        executionReporter.addExecutionNode("3", "2", "Given something");
        executionReporter.addExecutionNode("4", "2", "Then something else");
        executionReporter.resetExecutionState();
        executionReporter.allExecutionNodesAdded();
    }
}
