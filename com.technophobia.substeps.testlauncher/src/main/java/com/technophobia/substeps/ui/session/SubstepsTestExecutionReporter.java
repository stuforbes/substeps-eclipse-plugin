package com.technophobia.substeps.ui.session;

public interface SubstepsTestExecutionReporter {

    public enum ExecutionNodeType {
        ROOT, //
        FEATURE, //
        SCENARIO, //
        STEP
    };


    void addExecutionNode(String id, String parentNodeId, String text);


    void allExecutionNodesAdded();


    void executingNode(String id);


    void nodeCompleted(String id);


    void nodeFailed(String id);


    void resetExecutionState();


    void updateExecutingProject(String projectName);
}
