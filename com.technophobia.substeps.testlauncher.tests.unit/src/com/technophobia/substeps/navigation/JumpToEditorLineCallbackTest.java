package com.technophobia.substeps.navigation;

import static com.technophobia.substeps.navigation.JumpToEditorLineCallback.COMMAND;
import static com.technophobia.substeps.navigation.JumpToEditorLineCallback.CURRENT_PROJECT_PARAM;
import static com.technophobia.substeps.navigation.JumpToEditorLineCallback.DEFINITION_LINE_PARAM;

import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.technophobia.substeps.supplier.Callback2;

@RunWith(JMock.class)
public class JumpToEditorLineCallbackTest {

    private Mockery context;

    private IWorkbenchPartSite site;
    private ICommandService commandService;
    private IHandlerService handlerService;

    private Callback2<IProject, String> callback;


    @Before
    public void initialise() {
        this.context = new Mockery();

        this.site = context.mock(IWorkbenchPartSite.class);
        this.commandService = context.mock(ICommandService.class);
        this.handlerService = context.mock(IHandlerService.class);

        this.callback = new JumpToEditorLineCallback(site);
    }


    @Test
    public void executesCommandWithCorrectParameters() throws Exception {
        final String line = "This is a substep";
        final String projectName = "Project 1";

        final IProject project = context.mock(IProject.class);

        final ParameterizedCommand command = new ParameterizedCommand(new CommandManager().getCommand("test"), null);

        context.checking(new Expectations() {
            {
                oneOf(site).getService(ICommandService.class);
                will(returnValue(commandService));

                oneOf(site).getService(IHandlerService.class);
                will(returnValue(handlerService));

                oneOf(project).getName();
                will(returnValue(projectName));

                oneOf(commandService).deserialize(
                        COMMAND + "(" + CURRENT_PROJECT_PARAM + "=" + projectName + "," + DEFINITION_LINE_PARAM + "="
                                + line + ")");
                will(returnValue(command));

                oneOf(handlerService).executeCommand(command, null);
            }
        });

        callback.doCallback(project, line);
    }
}
