package com.technophobia.substeps.ui.session;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.technophobia.substeps.model.SubstepsSessionListener;
import com.technophobia.substeps.model.structure.SubstepsTestElement;
import com.technophobia.substeps.model.structure.SubstepsTestParentElement;

@RunWith(JMock.class)
public class UpdateViewWithSessionListenerTest {

    private Mockery context;

    private SubstepsTestExecutionReporter executionReporter;

    private SubstepsSessionListener sessionListener;


    @Before
    public void initialise() {
        this.context = new Mockery();

        this.executionReporter = context.mock(SubstepsTestExecutionReporter.class);

        this.sessionListener = new UpdateViewWithSessionListener(executionReporter);
    }


    @Test
    public void addingRootTestNodeUpdatesExecutionReporterCorrectly() {
        checkNodeAddedIs("1234", null, "Feature:");
    }


    @Test
    public void addingTestNodeUpdatesExecutionReporterCorrectly() {
        checkNodeAddedIs("2345", "1234", "Scenario: This is a scenario");
    }


    @Test
    public void beginningExecutionAlertsReporter() {
        context.checking(new Expectations() {
            {
                oneOf(executionReporter).allExecutionNodesAdded();
            }
        });

        sessionListener.runningBegins();
    }


    @Test
    public void listenerOnlyNotifiesExecutionReportersOnceThatTestsAreStarting() {
        context.checking(new Expectations() {
            {
                oneOf(executionReporter).allExecutionNodesAdded();
            }
        });

        sessionListener.runningBegins();
        sessionListener.runningBegins();
    }


    @Test
    public void startingNewSessionResetsNotificationCountForExecutionReporters() {
        context.checking(new Expectations() {
            {
                oneOf(executionReporter).updateExecutingProject("A Project");

                exactly(2).of(executionReporter).allExecutionNodesAdded();
                oneOf(executionReporter).resetExecutionState();
            }
        });

        sessionListener.runningBegins();
        sessionListener.sessionStarted("A Project");
        sessionListener.runningBegins();
    }


    private void checkNodeAddedIs(final String id, final String parentNodeId, final String message) {
        final SubstepsTestElement testElement = createTestElement(id, parentNodeId, message);

        context.checking(new Expectations() {
            {
                oneOf(executionReporter).addExecutionNode(id, parentNodeId, message);
            }
        });

        sessionListener.testAdded(testElement);
    }


    private SubstepsTestElement createTestElement(final String id, final String parentNodeId, final String message) {
        final SubstepsTestElement testElement = context.mock(SubstepsTestElement.class, "testElement");
        final SubstepsTestParentElement parentElement = context.mock(SubstepsTestParentElement.class);

        context.checking(new Expectations() {
            {
                oneOf(testElement).getId();
                will(returnValue(id));

                oneOf(testElement).getTestName();
                will(returnValue(message));

                oneOf(testElement).getParent();
                will(returnValue(parentElement));

                oneOf(parentElement).getId();
                will(returnValue(parentNodeId));

            }
        });

        return testElement;
    }
}
