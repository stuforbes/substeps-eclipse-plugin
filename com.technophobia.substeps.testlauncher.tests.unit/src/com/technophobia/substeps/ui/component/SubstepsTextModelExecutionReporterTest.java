package com.technophobia.substeps.ui.component;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.technophobia.substeps.supplier.Callback1;
import com.technophobia.substeps.ui.session.SubstepsTestExecutionReporter;

@RunWith(JMock.class)
public class SubstepsTextModelExecutionReporterTest {

    private Mockery context;

    private Callback1<String> textModelAsStringCallback;

    private SubstepsTestExecutionReporter executionReporter;


    @SuppressWarnings("unchecked")
    @Before
    public void initialise() {
        this.context = new Mockery();

        this.textModelAsStringCallback = context.mock(Callback1.class);

        this.executionReporter = new SubstepsTextModelExecutionReporter(textModelAsStringCallback);
    }


    @Test
    public void noExecutionNodesProducesEmptyTextModelString() {

        context.checking(new Expectations() {
            {
                oneOf(textModelAsStringCallback).doCallback("");
            }
        });

        executionReporter.allExecutionNodesAdded();
    }


    @Test
    public void singleExecutionNodeProducesCorrectTextModelString() {
        context.checking(new Expectations() {
            {
                oneOf(textModelAsStringCallback).doCallback("Feature: This is a feature");
            }
        });

        executionReporter.addExecutionNode("1", null, "Feature: This is a feature");
        executionReporter.allExecutionNodesAdded();
    }


    @Test
    public void multipleExecutionNodesProduceCorrectTextModelString() {
        context.checking(new Expectations() {
            {
                oneOf(textModelAsStringCallback)
                        .doCallback(
                                "Feature: This is a feature\n\tScenario: A scenario\n\t\tGiven something\n\t\tThen something else");
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
        context.checking(new Expectations() {
            {
                oneOf(textModelAsStringCallback)
                        .doCallback(
                                "Feature: This is a feature\n\tScenario: A scenario\n\t\tGiven something\n\t\t\tGiven some substep\n\t\tThen something else");
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
                oneOf(textModelAsStringCallback).doCallback("");
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
