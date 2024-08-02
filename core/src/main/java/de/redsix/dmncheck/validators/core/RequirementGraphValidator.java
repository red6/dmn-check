package de.redsix.dmncheck.validators.core;

import de.redsix.dmncheck.drg.RequirementGraph;
import de.redsix.dmncheck.result.ValidationResult;
import java.util.Collections;
import java.util.List;
import org.camunda.bpm.model.dmn.DmnModelInstance;

/** Generic validator that facilitates writing validations on graph-based representation of the DMN model instance. */
public abstract class RequirementGraphValidator implements Validator {

    /**
     * Returns a list of validation results for a requirement graph (see {@link RequirementGraph}).
     *
     * @param drg Requirement graph used for validation
     * @return A possibly empty list of validation results
     */
    protected abstract List<ValidationResult> validate(RequirementGraph drg);

    @Override
    public List<ValidationResult> apply(DmnModelInstance dmnModelInstance) {
        try {
            final RequirementGraph requirementGraph = RequirementGraph.from(dmnModelInstance);
            return validate(requirementGraph);
        } catch (IllegalArgumentException exception) {
            return Collections.singletonList(ValidationResult.init
                    .message("Error while construction requirement graph: " + exception.getMessage())
                    .element(dmnModelInstance.getDefinitions())
                    .build());
        }
    }
}
