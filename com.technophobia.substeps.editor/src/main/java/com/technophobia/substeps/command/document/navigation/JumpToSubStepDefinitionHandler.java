/*******************************************************************************
 * Copyright Technophobia Ltd 2012
 * 
 * This file is part of the Substeps Eclipse Plugin.
 * 
 * The Substeps Eclipse Plugin is free software: you can redistribute it and/or modify
 * it under the terms of the Eclipse Public License v1.0.
 * 
 * The Substeps Eclipse Plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Eclipse Public License for more details.
 * 
 * You should have received a copy of the Eclipse Public License
 * along with the Substeps Eclipse Plugin.  If not, see <http://www.eclipse.org/legal/epl-v10.html>.
 ******************************************************************************/
package com.technophobia.substeps.command.document.navigation;

import static com.technophobia.substeps.FeatureEditorPlugin.instance;

import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IWorkbenchPage;

import com.technophobia.eclipse.transformer.ProjectToJavaProjectTransformer;
import com.technophobia.substeps.FeatureEditorPlugin;
import com.technophobia.substeps.command.AbstractSubstepsEditorHandler;
import com.technophobia.substeps.document.navigation.OpenJavaEditor;
import com.technophobia.substeps.document.navigation.OpenSubstepsEditor;
import com.technophobia.substeps.model.ParentStep;
import com.technophobia.substeps.model.StepImplementation;
import com.technophobia.substeps.model.Syntax;

public class JumpToSubStepDefinitionHandler extends AbstractSubstepsEditorHandler {

    private static final ProjectToJavaProjectTransformer PROJECT_TRANSFORMER = new ProjectToJavaProjectTransformer();


    @Override
    protected void doWithLine(final String line, final IProject project, final IWorkbenchPage page) {
        if (line != null) {
            instance().info("F3 lookup on line: " + line);

            // Set the Syntax from SubstepSuggestionProvider
            final Syntax syntax = FeatureEditorPlugin.instance().syntaxFor(project);

            // We can be finding definitions written in either a Substeps file
            // or an annotated method in a Java class, these correspond to a
            // ParentStep or a StepImplementation in the Syntax respectively.
            final ParentStep parentStep = findParent(syntax, line);
            if (parentStep != null) {
                // Open the user defined Substep file.
                OpenSubstepsEditor.open(page, PROJECT_TRANSFORMER.from(project), parentStep);
            } else {
                final StepImplementation stepImplementation = findStep(syntax, line);
                if (stepImplementation != null) {
                    OpenJavaEditor.open(PROJECT_TRANSFORMER.from(project), stepImplementation.getMethod());
                }
            }
        }
    }


    private static final ParentStep findParent(final Syntax syntax, final String stepValue) {
        for (final ParentStep rootSubStep : syntax.getSortedRootSubSteps()) {
            if (Pattern.matches(rootSubStep.getParent().getPattern(), stepValue)) {
                return rootSubStep;
            }
        }
        return null;
    }


    private static final StepImplementation findStep(final Syntax syntax, final String stepValue) {
        for (final StepImplementation stepImplementation : syntax.getStepImplementations()) {
            if (Pattern.matches(stepImplementation.getValue(), stepValue)) {
                return stepImplementation;
            }
        }
        return null;
    }
}
