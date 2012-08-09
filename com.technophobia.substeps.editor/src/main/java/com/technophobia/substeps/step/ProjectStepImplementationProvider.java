package com.technophobia.substeps.step;

import java.util.Collection;

public interface ProjectStepImplementationProvider extends ProjectSuggestionProvider {

    Collection<Class<?>> stepImplementationClasses();
}
