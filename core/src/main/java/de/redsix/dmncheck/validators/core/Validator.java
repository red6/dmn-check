package de.redsix.dmncheck.validators.core;

import de.redsix.dmncheck.result.ValidationResult;
import java.util.List;
import org.camunda.bpm.model.dmn.DmnModelInstance;

/**
 * Generic interface for the validation of DMN model instances.
 *
 * <p>Every validation for dmn-check has to implement this interface.
 */
public interface Validator {

    /**
     * Validates the given DMN model instance and returns the validation results as a list.
     *
     * @param dmnModelInstance DMN model instance used for validation
     * @return A possibly empty list of validation results
     */
    List<ValidationResult> apply(final DmnModelInstance dmnModelInstance);
}
