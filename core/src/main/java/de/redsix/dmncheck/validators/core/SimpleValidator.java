package de.redsix.dmncheck.validators.core;

import org.camunda.bpm.model.xml.instance.ModelElementInstance;

/**
 * A simple validator is a {@link GenericValidator} where the validation (T) and applicability check (S) type are always
 * the same.
 *
 * @param <T> Class that is used for validation and for the applicability check
 */
public abstract class SimpleValidator<T extends ModelElementInstance> extends GenericValidator<T, T> {

    @Override
    public Class<T> getClassUsedToCheckApplicability() {
        return getClassUnderValidation();
    }
}
