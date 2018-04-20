package de.redsix.dmncheck.validators.core;

import org.camunda.bpm.model.xml.instance.ModelElementInstance;

public abstract class SimpleValidator<T extends ModelElementInstance> extends GenericValidator<T, T>{

    @Override
    public Class<T> getClassUsedToCheckApplicability() {
        return getClassUnderValidation();
    }
}
