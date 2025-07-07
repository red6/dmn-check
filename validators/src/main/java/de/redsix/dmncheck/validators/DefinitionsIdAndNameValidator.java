package de.redsix.dmncheck.validators;

import org.camunda.bpm.model.dmn.instance.Definitions;

public class DefinitionsIdAndNameValidator
    extends IdAndNameValidator<Definitions> {

    @Override
    public String getName() {
        return "definitions";
    }

    @Override
    public Class<Definitions> getClassUnderValidation() {
        return Definitions.class;
    }
}
