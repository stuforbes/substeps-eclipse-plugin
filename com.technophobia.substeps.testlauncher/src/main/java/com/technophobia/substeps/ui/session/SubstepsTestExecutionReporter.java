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


    void resetExecutionState();
}
