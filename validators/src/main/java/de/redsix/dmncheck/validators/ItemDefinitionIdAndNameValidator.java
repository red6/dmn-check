package de.redsix.dmncheck.validators;

import org.camunda.bpm.model.dmn.instance.ItemDefinition;

public class ItemDefinitionIdAndNameValidator
    extends IdAndNameValidator<ItemDefinition> {

    @Override
    protected String getName() {
        return "item definition";
    }

    @Override
    protected Class<ItemDefinition> getClassUnderValidation() {
        return ItemDefinition.class;
    }
}
