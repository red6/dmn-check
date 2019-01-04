package de.redsix.dmncheck.validators;

import org.camunda.bpm.model.dmn.instance.Decision;

public class DecisionIdAndNameValidator extends IdAndNameValidator<Decision> {

    @Override
    public String getName() {
        return "decision";
    }

    @Override
    public Class<Decision> getClassUnderValidation() {
        return Decision.class;
    }
}
