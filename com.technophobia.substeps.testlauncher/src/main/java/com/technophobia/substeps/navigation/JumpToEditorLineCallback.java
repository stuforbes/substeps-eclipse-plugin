package com.technophobia.substeps.navigation;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.SerializationException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;

import com.technophobia.substeps.FeatureRunnerPlugin;
import com.technophobia.substeps.supplier.Callback2;

public class JumpToEditorLineCallback implements Callback2<IProject, String> {

    static final String COMMAND = "com.technophobia.substeps.editor.navigate.to.feature.line";
    static final String CURRENT_PROJECT_PARAM = "com.technophobia.substeps.editor.navigate.to.feature.line.current.project";
    static final String DEFINITION_LINE_PARAM = "com.technophobia.substeps.editor.navigate.to.feature.line.definition.line";

    private final IWorkbenchPartSite site;


    public JumpToEditorLineCallback(final IWorkbenchPartSite site) {
        this.site = site;
    }


    @Override
    public void doCallback(final IProject project, final String line) {
        final ParameterizedCommand command = buildCommandForProjectAndLine(project, line);

        if (command != null) {
            executeCommand(command);
        }
    }


    private ParameterizedCommand buildCommandForProjectAndLine(final IProject project, final String line) {
        final ICommandService commandService = (ICommandService) site.getService(ICommandService.class);

        try {
            return commandService.deserialize(buildCommandStringFor(project.getName(), line));
        } catch (final NotDefinedException ex) {
            FeatureRunnerPlugin.error("Could not execute Jump to feature line command for line " + line, ex);
        } catch (final SerializationException ex) {
            FeatureRunnerPlugin.error("Could not execute Jump to feature line command for line " + line, ex);
        }
        return null;
    }


    private void executeCommand(final ParameterizedCommand command) {
        final IHandlerService handlerService = (IHandlerService) site.getService(IHandlerService.class);

        try {
            handlerService.executeCommand(command, null);
        } catch (final ExecutionException ex) {
            FeatureRunnerPlugin.error("Could not execute Jump to feature line command for command " + command, ex);
        } catch (final NotDefinedException ex) {
            FeatureRunnerPlugin.error("Could not execute Jump to feature line command for command " + command, ex);
        } catch (final NotEnabledException ex) {
            FeatureRunnerPlugin.error("Could not execute Jump to feature line command for command " + command, ex);
        } catch (final NotHandledException ex) {
            FeatureRunnerPlugin.error("Could not execute Jump to feature line command for command " + command, ex);
        }
    }


    private String buildCommandStringFor(final String projectName, final String line) {
        final StringBuilder sb = new StringBuilder();
        sb.append(COMMAND);
        sb.append("(");
        addCommandParam(CURRENT_PROJECT_PARAM, projectName, sb);
        sb.append(",");
        addCommandParam(DEFINITION_LINE_PARAM, line, sb);
        sb.append(")");
        return sb.toString();
    }


    private void addCommandParam(final String paramName, final String paramValue, final StringBuilder sb) {
        sb.append(paramName);
        sb.append("=");
        sb.append(paramValue);
    }
}
