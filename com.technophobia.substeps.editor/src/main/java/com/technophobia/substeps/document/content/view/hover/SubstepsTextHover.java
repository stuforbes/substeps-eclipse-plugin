package com.technophobia.substeps.document.content.view.hover;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.technophobia.substeps.FeatureEditorPlugin;
import com.technophobia.substeps.document.content.view.hover.model.HoverModel;
import com.technophobia.substeps.document.content.view.hover.model.SubstepHoverModel;
import com.technophobia.substeps.model.ParentStep;
import com.technophobia.substeps.model.StepImplementation;
import com.technophobia.substeps.model.Syntax;
import com.technophobia.substeps.supplier.DefaultMutableItemContainer;
import com.technophobia.substeps.supplier.MutableSupplier;
import com.technophobia.substeps.supplier.Supplier;

public class SubstepsTextHover implements ITextHover, ITextHoverExtension {

    private final Supplier<IProject> currentProjectSupplier;


    public SubstepsTextHover(final Supplier<IProject> currentProjectSupplier) {
        this.currentProjectSupplier = currentProjectSupplier;
    }


    @Override
    public String getHoverInfo(final ITextViewer textViewer, final IRegion hoverRegion) {
        final IProject project = currentProject();

        final String line = lineFor(textViewer, hoverRegion);

        final Syntax syntax = FeatureEditorPlugin.instance().syntaxFor(project);

        final HoverModel hoverModel = hoverModelAtLine(line, syntax);
        if (hoverModel != null) {
            return hoverModel.serializeToString();
        }
        return null;
    }


    private HoverModel hoverModelAtLine(final String line, final Syntax syntax) {
        final List<ParentStep> substeps = substepsForLine(line, syntax);
        if (substeps != null && !substeps.isEmpty()) {
            return new SubstepHoverModel(substeps.get(0));
        } else if (isStepImpl(line, syntax)) {
            // return stepImplInfo(line, syntax);
        }
        return null;
    }


    @Override
    public IRegion getHoverRegion(final ITextViewer textViewer, final int offset) {
        final IDocument document = textViewer.getDocument();

        return getLineRegionAtOffset(document, offset);
    }


    @Override
    public IInformationControlCreator getHoverControlCreator() {
        return new IInformationControlCreator() {

            @Override
            public IInformationControl createInformationControl(final Shell parent) {
                return new SubstepsHoverControl(parent);
            }
        };
    }


    private List<ParentStep> substepsForLine(final String line, final Syntax syntax) {
        return syntax.getSubStepsMap().get(line);
    }


    private boolean isStepImpl(final String line, final Syntax syntax) {
        return syntax.getStepImplementationMap().containsKey(line);
    }


    private String stepImplInfo(final String line, final Syntax syntax) {
        final List<StepImplementation> list = syntax.getStepImplementationMap().get(line).get(line);
        final StringBuilder sb = new StringBuilder();
        for (final StepImplementation step : list) {
            sb.append(step.getImplementedIn().getName());
        }
        return sb.toString();
    }


    private String lineFor(final ITextViewer textViewer, final IRegion region) {
        try {
            return textViewer.getDocument().get(region.getOffset(), region.getLength()).trim();
        } catch (final BadLocationException e) {
            FeatureEditorPlugin.instance().error(
                    "Couldn't get line at region (" + region.getOffset() + ", " + region.getLength() + ")");
        }
        return null;
    }


    private IRegion getLineRegionAtOffset(final IDocument document, final int offset) {
        try {
            return document.getLineInformationOfOffset(offset);
        } catch (final BadLocationException ex) {
            FeatureEditorPlugin.instance().error("Could not find line for offset " + offset, ex);
        }
        return null;
    }


    private IProject currentProject() {
        final MutableSupplier<IProject> projectSupplier = new DefaultMutableItemContainer<IProject>();
        Display.getDefault().syncExec(new Runnable() {

            @Override
            public void run() {
                projectSupplier.set(currentProjectSupplier.get());
            }

        });
        return projectSupplier.get();
    }
}
