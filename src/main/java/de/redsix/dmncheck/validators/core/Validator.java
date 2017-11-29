package de.redsix.dmncheck.validators.core;

import org.camunda.bpm.model.xml.instance.ModelElementInstance;

public interface Validator<T extends ModelElementInstance> extends GenericValidator<T, T>{

    @Override
    default Class<T> getClassUsedToCheckApplicability() {
        return getClassUnderValidation();
    }
}
