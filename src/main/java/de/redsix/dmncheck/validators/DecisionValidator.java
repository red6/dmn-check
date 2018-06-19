package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.core.SimpleValidator;
import org.camunda.bpm.model.dmn.instance.Decision;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DecisionValidator extends SimpleValidator<Decision> {

    @Override
    public boolean isApplicable(Decision decision) {
        return true;
    }

    @Override
    public List<ValidationResult> validate(Decision decision) {
        final List<ValidationResult> validationResults = new ArrayList<>();

        if (Objects.isNull(decision.getId())) {
            validationResults.add(ValidationResult.init
                    .message("A decision has no id.")
                    .element(decision)
                    .build());
        }

        if (Objects.isNull(decision.getName())) {
            validationResults.add(ValidationResult.init
                    .message("A decision has no name.")
                    .severity(Severity.WARNING)
                    .element(decision)
                    .build());
        }

        return validationResults;
    }

    @Override
    public Class<Decision> getClassUnderValidation() {
        return Decision.class;
    }
}
