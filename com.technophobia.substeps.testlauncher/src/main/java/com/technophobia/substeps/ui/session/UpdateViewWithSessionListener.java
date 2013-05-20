package com.technophobia.substeps.ui.session;

import org.eclipse.core.runtime.IStatus;

import com.technophobia.substeps.FeatureRunnerPlugin;
import com.technophobia.substeps.junit.ui.SubstepsRunSession;
import com.technophobia.substeps.model.SubstepsSessionListener;
import com.technophobia.substeps.model.structure.Status;
import com.technophobia.substeps.model.structure.SubstepsTestElement;
import com.technophobia.substeps.model.structure.SubstepsTestLeafElement;

public class UpdateViewWithSessionListener implements SubstepsSessionListener {

    private final SubstepsTestExecutionReporter executionReporter;

    private boolean runningStarted;


    public UpdateViewWithSessionListener(final SubstepsTestExecutionReporter executionReporter) {
        FeatureRunnerPlugin.log(IStatus.INFO, "New session listener");
        this.executionReporter = executionReporter;
        this.runningStarted = false;
    }


    @Override
    public void sessionStarted(final String projectName) {
        this.runningStarted = false;
        this.executionReporter.resetExecutionState();
        this.executionReporter.updateExecutingProject(projectName);
    }


    @Override
    public void sessionEnded(final long elapsedTime) {
        // substepsFeatureTestRunnerViewPart.addListItem("Session ended after" +
        // elapsedTime);
    }


    @Override
    public void sessionStopped(final long elapsedTime) {
        // substepsFeatureTestRunnerViewPart.addListItem("Session stopped after "
        // + elapsedTime);
    }


    @Override
    public void sessionTerminated() {
        // substepsFeatureTestRunnerViewPart.addListItem("Session terminated");
    }


    @Override
    public void testAdded(final SubstepsTestElement testElement) {
        final String testName = testElement.getTestName();
        executionReporter.addExecutionNode(testElement.getId(), testElement.getParent().getId(), testName);
    }


    @Override
    public void runningBegins() {
        if (!runningStarted) {
            // we should only do this once. Unfortunately, this method is called
            // for each test element
            executionReporter.allExecutionNodesAdded();
            runningStarted = true;
        }
    }


    @Override
    public void testStarted(final SubstepsTestLeafElement testCaseElement) {
        executionReporter.executingNode(testCaseElement.getId());
    }


    @Override
    public void testEnded(final SubstepsTestLeafElement testCaseElement) {
        executionReporter.nodeCompleted(testCaseElement.getId());
    }


    @Override
    public void testFailed(final SubstepsTestElement testElement, final Status status, final String trace,
            final String expected, final String actual) {
        if (Status.FAILURE.equals(status)) {
            executionReporter.nodeFailed(testElement.getId(), expected, actual);
        } else {
            executionReporter.nodeError(testElement.getId(), trace);
        }
    }


    @Override
    public void testReran(final SubstepsTestLeafElement testCaseElement, final Status status, final String trace,
            final String expectedResult, final String actualResult) {
        // substepsFeatureTestRunnerViewPart.addListItem("Test reran " +
        // testCaseElement.getTestName());
    }


    @Override
    public boolean acceptsSwapToDisk() {
        return false;
    }


    @Override
    public void sessionLaunched(final SubstepsRunSession substepsRunSession) {
        // substepsFeatureTestRunnerViewPart.addListItem("Session launched");
    }
}
