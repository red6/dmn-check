package de.redsix.dmncheck.validators;

import org.camunda.bpm.model.dmn.instance.InputData;

public class InputDataIdAndNameValidator extends IdAndNameValidator<InputData> {

    @Override
    public String getName() {
        return "input";
    }

    @Override
    public Class<InputData> getClassUnderValidation() {
        return InputData.class;
    }
}
