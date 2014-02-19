package com.technophobia.substeps.view.dependencies;

import java.io.File;
import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

import com.google.common.base.Supplier;
import com.technophobia.eclipse.project.ProjectManager;
import com.technophobia.substeps.FeatureEditorPlugin;
import com.technophobia.substeps.editor.message.SubstepsEditorMessages;
import com.technophobia.substeps.execution.node.IExecutionNode;
import com.technophobia.substeps.execution.node.StepNode;
import com.technophobia.substeps.model.Syntax;
import com.technophobia.substeps.runner.usage.ExecutionNodeStepUsageAnalyser;
import com.technophobia.substeps.runner.usage.ExecutionNodeStepUsageVisitor;
import com.technophobia.substeps.runner.usage.FeatureFolderAndSyntaxRootNodeSupplier;
import com.technophobia.substeps.runner.usage.StatefulExecutionNodeVisitor;

public class ShowDependenciesViewPart extends ViewPart {

    public static final String NAME = "com.technophobia.substeps.view.SubstepsDependenciesView";

    private Composite content;
    private StepDependenciesContentProvider contentProvider;

    private TreeViewer treeViewer;


    @Override
    public void createPartControl(final Composite parent) {
        content = new Composite(parent, SWT.NONE);
        content.setLayout(new StackLayout());

        final Label label = new Label(content, SWT.NONE);
        label.setText(SubstepsEditorMessages.ShowDependencesView_No_Step);

        treeViewer = new TreeViewer(content, SWT.NONE);
        contentProvider = new StepDependenciesContentProvider();
        treeViewer.setContentProvider(contentProvider);
        treeViewer.setLabelProvider(new StepDependenciesLabelProvider());
        treeViewer.setInput(null);

        switchContentTo(0);
    }


    @Override
    public void setFocus() {
        // TODO Auto-generated method stub

    }


    @Override
    public void dispose() {
        contentProvider.dispose();
        content.dispose();
        super.dispose();
    }


    public void showDependenciesForLine(final String line, final IProject project) {
        switchContentTo(1);

        final String processedLine = removeDirectivesFrom(line);

        final ProjectManager projectManager = FeatureEditorPlugin.instance().projectManager();
        final File featureFolder = projectManager.featureFolderFor(project).toFile();

        final Syntax syntax = FeatureEditorPlugin.instance().syntaxFor(project);

        final ExecutionNodeStepUsageAnalyser stepUsageAnalyser = new ExecutionNodeStepUsageAnalyser(
                new FeatureFolderAndSyntaxRootNodeSupplier(featureFolder.getAbsolutePath(), syntax),
                new Supplier<StatefulExecutionNodeVisitor<UsageResult>>() {
                };

        final Collection<StepNode> stepNodes = stepUsageAnalyser.stepsWithLine(processedLine);
        printSteps(stepNodes.toArray(new StepNode[0]));
        treeViewer.setInput(stepNodes);

        updateTree();
    }


    private void printSteps(final IExecutionNode... nodes) {
        for (final IExecutionNode node : nodes) {
            System.out.println(node.getClass().getName() + ": " + node.getDescription());
        }
    }


    private void updateTree() {
        // set the input so that the outlines parse can be called
        // update the tree viewer state
        if (treeViewer != null) {
            final Control control = treeViewer.getControl();
            treeViewer.refresh();
            if (control != null && !control.isDisposed()) {
                control.setRedraw(false);
                treeViewer.expandAll();
                control.setRedraw(true);
            }
        }
    }


    private void printParentAndNode(final IExecutionNode node) {
        if (node.getParent() != null) {
            printParentAndNode(node.getParent());
        }
        System.out.print(node.getDescription() + " -> ");
    }


    private void switchContentTo(final int position) {
        ((StackLayout) content.getLayout()).topControl = content.getChildren()[position];
        content.layout();
    }


    private String removeDirectivesFrom(final String line) {
        if (line.startsWith("Define:")) {
            return line.substring(7).trim();
        }
        return line.trim();
    }
}