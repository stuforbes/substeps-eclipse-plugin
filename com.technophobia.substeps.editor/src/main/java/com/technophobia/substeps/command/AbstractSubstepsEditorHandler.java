package com.technophobia.substeps.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.technophobia.substeps.FeatureEditorPlugin;
import com.technophobia.substeps.editor.FeatureEditor;

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
            final IContainer container = file.getParent();

            final IProject project = container.getProject();

            final FeatureEditor currentEditor = (FeatureEditor) activeEditor.getAdapter(FeatureEditor.class);

            final IDocument currentDocument = currentEditor.getCurrentDocument();

            final IEditorSite editorSite = activeEditor.getEditorSite();

            if (editorSite != null) {
                final ISelectionProvider selectionProvider = editorSite.getSelectionProvider();

                if (selectionProvider != null) {
                    doWithLine(getSelectedLine((ITextSelection) selectionProvider.getSelection(), currentDocument),
                            project, page);
                }
            } else {
                FeatureEditorPlugin.instance().warn(
                        "Trying to invoke command [" + this.getClass().getName() + "] with a null EditorSite");
            }
        } else {
            FeatureEditorPlugin.instance().warn(
                    "Trying to invoke command [" + this.getClass().getName() + "] with file [" + file.getName()
                            + "]. Only feature files or substep files are allowed");
        }

        // Must return null, apparently
        return null;
    }


    private String getSelectedLine(final ITextSelection selection, final IDocument currentDocument) {
        String rtn = null;

        final int offset = selection.getOffset();
        try {
            final int lineNumber = currentDocument.getLineOfOffset(offset);
            final int lineStart = currentDocument.getLineOffset(lineNumber);

            String line = currentDocument.get(lineStart, currentDocument.getLineLength(lineNumber));

            final int commentIdx = line.indexOf("#");

            if (commentIdx >= 0) {
                line = line.substring(0, commentIdx);
            }

            rtn = line.trim();

        } catch (final BadLocationException e) {
            FeatureEditorPlugin.instance().error("BadLocationException getting current line @offset: " + offset, e);
        }

        return rtn;
    }


    protected abstract void doWithLine(String line, IProject project, IWorkbenchPage page);
}
