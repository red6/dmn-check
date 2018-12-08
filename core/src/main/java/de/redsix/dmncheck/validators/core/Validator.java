package de.redsix.dmncheck.validators.core;

import de.redsix.dmncheck.result.ValidationResult;
import org.camunda.bpm.model.dmn.DmnModelInstance;

import java.util.List;

public interface Validator {
    List<ValidationResult> apply(final DmnModelInstance dmnModelInstance);
}
