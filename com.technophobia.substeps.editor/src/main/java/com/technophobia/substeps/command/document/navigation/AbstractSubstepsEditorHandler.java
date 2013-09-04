package com.technophobia.substeps.command.document.navigation;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

public abstract class AbstractSubstepsEditorHandler extends AbstractHandler {

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

        final IWorkbenchPage page = window.getActivePage();

        // TextEditor implements IEditorPart
        final IEditorPart activeEditor = page.getActiveEditor();

        final IEditorInput editorInput = activeEditor.getEditorInput();

        final IFile file = (IFile) editorInput.getAdapter(IFile.class);

        final boolean isFeatureFile = file.getFileExtension().toLowerCase().equals("feature");
        final boolean isSubstepsFile = file.getFileExtension().toLowerCase().equals("substeps");

        if (isFeatureFile || isSubstepsFile) {

        }
    }

}
