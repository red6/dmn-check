package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.core.SimpleValidator;
import de.redsix.dmncheck.validators.core.ValidationContext;
import org.camunda.bpm.model.dmn.instance.Decision;
import org.camunda.bpm.model.dmn.instance.Definitions;

import java.util.Collections;
import java.util.List;

public class NoDecisionPresentValidator extends SimpleValidator<Definitions> {

    @Override
    public boolean isApplicable(Definitions definitions, ValidationContext validationContext) {
        return true;
    }

    @Override
    public List<ValidationResult> validate(Definitions definitions, ValidationContext validationContext) {
        if (definitions.getChildElementsByType(Decision.class).isEmpty()) {
            return Collections.singletonList(ValidationResult.init
                    .message("No decisions found")
                    .severity(Severity.WARNING)
                    .element(definitions)
                    .build());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Class<Definitions> getClassUnderValidation() {
        return Definitions.class;
    }
}
