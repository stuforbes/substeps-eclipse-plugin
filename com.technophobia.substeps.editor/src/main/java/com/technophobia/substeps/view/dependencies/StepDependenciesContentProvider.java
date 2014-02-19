package com.technophobia.substeps.view.dependencies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.technophobia.substeps.FeatureEditorPlugin;
import com.technophobia.substeps.execution.node.FeatureNode;
import com.technophobia.substeps.execution.node.IExecutionNode;
import com.technophobia.substeps.execution.node.NodeWithChildren;
import com.technophobia.substeps.execution.node.StepNode;

public class StepDependenciesContentProvider implements ITreeContentProvider {

    private List<FeatureNode> features;


    public StepDependenciesContentProvider() {
        this.features = new ArrayList<FeatureNode>();
    }


    @Override
    public void dispose() {
        features = null;
    }


    @SuppressWarnings("unchecked")
    @Override
    public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
        if (newInput != null) {
            updateContentWith((Collection<StepNode>) newInput);
        }
    }


    @Override
    public Object[] getElements(final Object inputElement) {
        if (features != null) {
            return features.toArray(new FeatureNode[features.size()]);
        }
        return new FeatureNode[0];
    }


    @Override
    public Object[] getChildren(final Object parentElement) {
        if (parentElement instanceof NodeWithChildren<?>) {
            final List<?> children = ((NodeWithChildren<?>) parentElement).getChildren();
            return children.toArray(new Object[children.size()]);
        }
        return new Object[0];
    }


    @Override
    public Object getParent(final Object element) {
        if (element instanceof IExecutionNode) {
            return ((IExecutionNode) element).getParent();
        }
        return null;
    }


    @Override
    public boolean hasChildren(final Object element) {
        return element instanceof NodeWithChildren<?> && ((NodeWithChildren<?>) element).hasChildren();
    }


    private void updateContentWith(final Collection<StepNode> stepNodes) {
        final Map<Long, FeatureNode> idToFeatureNodeMap = new HashMap<Long, FeatureNode>();
        int foundFeature = 0;
        int notFoundFeature = 0;

        for (final StepNode stepNode : stepNodes) {
            final FeatureNode featureNode = fromStepNode(stepNode);
            if (featureNode != null) {
                foundFeature++;
                final Long featureNodeId = Long.valueOf(featureNode.getId());
                if (!idToFeatureNodeMap.containsKey(featureNodeId)) {
                    idToFeatureNodeMap.put(featureNodeId, featureNode);
                } else {
                    mergeNodeFragmentInto(idToFeatureNodeMap.get(featureNodeId), featureNode);
                }
            } else {
                notFoundFeature++;
                FeatureEditorPlugin.instance().warn("Could not find feature file for step " + stepNode);
            }
        }
        System.out.println("Steps: " + stepNodes.size() + ". Found " + foundFeature + " features, failed to find "
                + notFoundFeature);
        features.clear();
        features.addAll(idToFeatureNodeMap.values());
    }


    private FeatureNode fromStepNode(final StepNode stepNode) {
        IExecutionNode node = stepNode;
        while (node != null && !(node instanceof FeatureNode)) {
            node = node.getParent();
        }

        return (FeatureNode) node;
    }


    @SuppressWarnings("unchecked")
    private void mergeNodeFragmentInto(final IExecutionNode master, final IExecutionNode fragment) {
        if (master.getId() != fragment.getId()) {
            FeatureEditorPlugin.instance().warn(
                    "Something went wrong trying to merge node fragment " + fragment + " into node tree " + master
                            + ". The ids did not match up");
        } else {
            if (master instanceof NodeWithChildren && fragment instanceof NodeWithChildren) {
                final List<IExecutionNode> masterChildren = ((NodeWithChildren<IExecutionNode>) master).getChildren();

                // fragment children will only have 1 child
                final IExecutionNode fragmentChild = ((NodeWithChildren<IExecutionNode>) fragment).getChildren().get(0);

                boolean foundMatch = false;
                for (final IExecutionNode masterChild : masterChildren) {
                    if (masterChild.getId() == fragmentChild.getId()) {
                        mergeNodeFragmentInto(masterChild, fragmentChild);
                        foundMatch = true;
                        break;
                    }
                }
                if (!foundMatch) {
                    // this fragment is new
                    ((NodeWithChildren<IExecutionNode>) master).getChildren().add(fragmentChild);
                }
            }
        }
    }
}
