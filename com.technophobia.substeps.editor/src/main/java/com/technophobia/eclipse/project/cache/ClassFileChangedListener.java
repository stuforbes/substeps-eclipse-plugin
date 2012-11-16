package com.technophobia.eclipse.project.cache;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;

import com.technophobia.substeps.FeatureEditorPlugin;
import com.technophobia.substeps.supplier.Callback1;

public class ClassFileChangedListener implements IElementChangedListener {

    private static final String CLASS_FILE_SUFFIX = "class";

    private final Callback1<IProject> projectChangedNotifier;


    public ClassFileChangedListener(final Callback1<IProject> projectChangedNotifier) {
        this.projectChangedNotifier = projectChangedNotifier;
    }


    @Override
    public void elementChanged(final ElementChangedEvent event) {
        if (event.getType() == ElementChangedEvent.POST_CHANGE) {
            final IJavaElementDelta delta = event.getDelta();
            final Collection<IJavaElementDelta> changeNodes = new ArrayList<IJavaElementDelta>();
            findChangeNodes(delta, changeNodes);

            for (final IJavaElementDelta changeNode : changeNodes) {
                final IJavaProject javaProject = changeNode.getElement().getJavaProject();
                if (isClassFileChange(changeNode, javaProject)) {
                    projectChangedNotifier.doCallback(javaProject.getProject());
                }
            }
        }
    }


    private boolean isClassFileChange(final IJavaElementDelta delta, final IJavaProject javaProject) {
        try {
            final IPath projectPath = javaProject.getPath();
            if (isDeltaForProject(delta, projectPath)) {
                return affectedClassFiles(delta).size() > 0;
            }
        } catch (final CoreException ex) {
            FeatureEditorPlugin.instance().error(
                    "Could not determine whether change delta " + delta + " contains a class file change", ex);
        }
        return false;
    }


    private Collection<IResource> affectedClassFiles(final IJavaElementDelta delta) throws CoreException {
        final Collection<IResource> affectedClassFiles = new ArrayList<IResource>();
        final IResourceDeltaVisitor visitor = filesWithExtensionVisitor(affectedClassFiles);
        for (final IResourceDelta resourceDelta : delta.getResourceDeltas()) {
            resourceDelta.accept(visitor);
        }
        return affectedClassFiles;
    }


    private boolean isDeltaForProject(final IJavaElementDelta delta, final IPath projectPath) {
        return delta.getElement().getPath().equals(projectPath);
    }


    private void findChangeNodes(final IJavaElementDelta delta, final Collection<IJavaElementDelta> changeNodes) {
        if (hasFlag(IJavaElementDelta.CHANGED, delta.getKind())) {
            final int flags = delta.getFlags();

            if (hasFlag(IJavaElementDelta.F_CONTENT, flags)) {
                changeNodes.add(delta);
            } else if (hasFlag(IJavaElementDelta.F_CHILDREN, flags)) {
                for (final IJavaElementDelta child : delta.getAffectedChildren()) {
                    findChangeNodes(child, changeNodes);
                }
            }
        }
    }


    private boolean hasFlag(final int requiredFlag, final int flags) {
        return (flags & requiredFlag) == requiredFlag;
    }


    private IResourceDeltaVisitor filesWithExtensionVisitor(final Collection<IResource> changedResources) {
        return new IResourceDeltaVisitor() {

            @Override
            public boolean visit(final IResourceDelta delta) throws CoreException {
                if (delta.getKind() == IResourceDelta.CHANGED) {
                    if (delta.getResource().getFileExtension() != null
                            && CLASS_FILE_SUFFIX.equals(delta.getResource().getFileExtension().toLowerCase())) {
                        changedResources.add(delta.getResource());
                    }
                }
                return true;
            }
        };
    }
}
