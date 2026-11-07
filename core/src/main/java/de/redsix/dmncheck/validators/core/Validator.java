package de.redsix.dmncheck.validators.core;

import de.redsix.dmncheck.result.ValidationResult;
import java.util.List;
import java.util.Optional;
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

    /**
     * Link to a detailed description of the problems reported by this validator and how to solve them.
     *
     * @return URL of the documentation, if available
     */
    default Optional<String> documentation() {
        return Optional.empty();
    }

    /**
     * Validates the given DMN model instance and attaches the documentation of this validator to the results.
     *
     * @param dmnModelInstance DMN model instance used for validation
     * @return A possibly empty list of validation results
     */
    default List<ValidationResult> applyWithDocumentation(final DmnModelInstance dmnModelInstance) {
        return apply(dmnModelInstance).stream()
                .map(result -> documentation().map(result::withDocumentation).orElse(result))
                .toList();
    }
}
