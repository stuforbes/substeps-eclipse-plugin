package com.technophobia.substeps.document.navigation;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.imageio.stream.FileImageOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;

import com.technophobia.substeps.FeatureEditorPlugin;
import com.technophobia.substeps.document.content.assist.feature.ProjectToSyntaxTransformer;
import com.technophobia.substeps.editor.FeatureEditor;
import com.technophobia.substeps.editor.SubstepsEditor;
import com.technophobia.substeps.model.ParentStep;
import com.technophobia.substeps.model.Syntax;

/**
 * 
 * @author imoore
 * 
 */
public class JumpToSubStepDefinitionHandler extends AbstractHandler {

    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

        final IWorkbenchPage page = window.getActivePage();

        // textEditor implements IEditorPart
        final IEditorPart activeEditor = page.getActiveEditor();

        final IEditorInput editorInput = activeEditor.getEditorInput();

        final IFile file = (IFile) editorInput.getAdapter(IFile.class);

        final boolean isFeatureFile = file.getName().endsWith("feature");

        if (isFeatureFile) {

            final IContainer container = file.getParent();

            final IProject project = container.getProject();

            final FeatureEditor currentEditor = (FeatureEditor) activeEditor.getAdapter(FeatureEditor.class);

            final IDocument currentDocument = currentEditor.getCurrentDocument();

            final IEditorSite editorSite = activeEditor.getEditorSite();

            if (editorSite != null) {
                final ISelectionProvider selectionProvider = editorSite.getSelectionProvider();

                if (selectionProvider != null) {
                    jumpToStepDefinition(page, project, currentDocument, selectionProvider);
                }
            }
        }
        // must return null, apparently
        return null;
    }


    private void jumpToStepDefinition(final IWorkbenchPage page, final IProject project,
            final IDocument currentDocument, final ISelectionProvider selectionProvider) {
        final ITextSelection iSelection = (ITextSelection) selectionProvider.getSelection();

        final int offset = iSelection.getOffset();

        // get the line from where we are:

        final String currentLine = getCurrentLine(currentDocument, offset);

        if (currentLine != null) {

            FeatureEditorPlugin.info("F3 lookup on line: " + currentLine);

            // find this in the corresponding substeps file

            // from SubstepSuggestionProvider
            final ProjectToSyntaxTransformer projectToSyntaxTransformer = new ProjectToSyntaxTransformer();
            final Syntax syntax = projectToSyntaxTransformer.from(project);

            final List<ParentStep> substeps = syntax.getSortedRootSubSteps();

            ParentStep substepDefinition = null;
            
            for (final ParentStep rootSubStep : substeps) {
                // match on the pattern - take into account any
                // parameters
                if (Pattern.matches(rootSubStep.getParent().getPattern(), currentLine)) {

                    substepDefinition = rootSubStep;
                    break;
                }
            }
            
            IFile substepsIFile = null;
            
            if (substepDefinition != null) {

                substepsIFile = getSubstepIFile(project, substepDefinition.getParent().getSource());

                int lineNumber = substepDefinition.getSourceLineNumber();

                FeatureEditorPlugin.info("rootSubStep.getSourceLineNumber: " + lineNumber + " for line: "
                        + substepDefinition.getParent().getLine());

                final IEditorInput input = new FileEditorInput(substepsIFile);

                try {
                    final IEditorPart substepsEditor = page.openEditor(input,
                            "com.technophobia.substeps.editor.substepsEditor");

                    selectLineInEditor(lineNumber, substepsEditor);

                } catch (final PartInitException e) {
                    FeatureEditorPlugin.error("exception opening substep", e);
                }
            }
        }
    }


    private void selectLineInEditor(int lineNumber, final IEditorPart newEditor) {
        final ISelectionProvider newSelectionProvider = newEditor.getEditorSite().getSelectionProvider();

        final SubstepsEditor newStepEditor = (SubstepsEditor) newEditor.getAdapter(SubstepsEditor.class);

        // TODO might need to add a
        // navigationLocation in order to support
        // the eclipse back buttons
        // final INavigationLocation
        // navigationLocation =
        // newStepEditor.createNavigationLocation();

        final IDocument newDocument = newStepEditor.getCurrentDocument();

        try {
            // this is zero based
            IRegion lineInformation = newDocument.getLineInformation(lineNumber - 1);

            final int newOffset = lineInformation.getOffset();

            final int selectionLength = lineInformation.getLength();

            newSelectionProvider.setSelection(new TextSelection(newDocument, newOffset, selectionLength));

        } catch (BadLocationException e) {
            FeatureEditorPlugin.error("BadLocationException@ " + lineNumber, e);
        }
    }


    private IFile getSubstepIFile(final IProject project, File substepDefinitionFile) {
       
        IFile substepsIFile = null;
        // this file will be on the output path
        String substepOutputFile = substepDefinitionFile.getAbsolutePath();

        // we're only interested in the bit after the project

        IJavaProject javaProject = JavaCore.create(project);
        final Set<IPath> exclusionSet = new HashSet<IPath>();

        try {
            IClasspathEntry[] rawClasspath = javaProject.getRawClasspath();

            for (IClasspathEntry classpathEntry : rawClasspath) {

                if (classpathEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {

                    // outputLocation = workspace relative path
                    IPath outputLocation = classpathEntry.getOutputLocation();

                    if (outputLocation == null) {
                        outputLocation = javaProject.getOutputLocation();
                    }

                    exclusionSet.add(outputLocation);
                    // We will have searched here.

                    IPath projPath = project.getFullPath();

                    int matchSegs = outputLocation.matchingFirstSegments(projPath);

                    IPath removeFirstSegments = outputLocation.removeFirstSegments(matchSegs);

                    IFile outputFile = project.getFile(removeFirstSegments);

                    URI locationURI = outputFile.getLocationURI();

                    if (substepOutputFile.startsWith(locationURI.getPath())) {

                        // this is the ice we're interested in
                        String classpathRelativePath = substepOutputFile.substring(locationURI.getPath()
                                .length());

                        IFolder sourceIFolder = project.getFolder(classpathEntry.getPath().removeFirstSegments(1));
                        substepsIFile = sourceIFolder.getFile(classpathRelativePath);
                        if (substepsIFile.exists()) {
                            // Might not get a file
                           
                            break;
                        }
                    }
                }
            }
        } catch (JavaModelException e1) {
            FeatureEditorPlugin.error("JavaModelException", e1);
        }

        if (substepsIFile == null || !substepsIFile.exists()) {
            // Get the substeps file by searching.
            substepsIFile = searchForFile(project, substepDefinitionFile.getName(), substepsIFile, exclusionSet);
        }
        return substepsIFile;
    }


    private IFile searchForFile(final IProject project, String filename, IFile substepsIFile,
            final Set<IPath> exclusionSet) {
        final File file = new File(project.getLocationURI());
        IOFileFilter nameFileFilter = FileFilterUtils.nameFileFilter(filename);

        IOFileFilter dirnameFilter = FileFilterUtils.directoryFileFilter();
        for (IPath path : exclusionSet) {
            dirnameFilter = FileFilterUtils.and(dirnameFilter,
                    FileFilterUtils.notFileFilter(FileFilterUtils.nameFileFilter(path.lastSegment())));
        }

        Collection<File> listFiles = FileUtils.listFiles(file, nameFileFilter, dirnameFilter);
        if (listFiles.size() > 1) {
            throw new IllegalStateException("more than one file found"); // TODO
                                                                         // something
                                                                         // else?
        } else if (listFiles.size() == 1) {

            substepsIFile = project.getFile(listFiles.iterator().next().getAbsolutePath()
                    .substring(project.getLocationURI().getRawPath().length()));
        }
        return substepsIFile;
    }

    /**
     * @param currentDocument
     * @param offset
     * @return
     */
    private String getCurrentLine(final IDocument currentDocument, final int offset) {
        String rtn = null;

        int lineNumber;
        try {
            lineNumber = currentDocument.getLineOfOffset(offset);
            final int lineStart = currentDocument.getLineOffset(lineNumber);

            String line = currentDocument.get(lineStart, currentDocument.getLineLength(lineNumber));

            final int commentIdx = line.indexOf("#");

            if (commentIdx >= 0) {
                line = line.substring(0, commentIdx);
            }

            rtn = line.trim();

        } catch (final BadLocationException e) {
            FeatureEditorPlugin.error("BadLocationException getting current line @offset: " + offset, e);
        }

        return rtn;
    }

}
