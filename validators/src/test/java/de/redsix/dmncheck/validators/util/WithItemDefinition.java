package de.redsix.dmncheck.validators.util;

import org.camunda.bpm.model.dmn.instance.ItemDefinition;
import org.junit.jupiter.api.BeforeEach;

public class WithItemDefinition extends WithDefinitions {

    protected ItemDefinition itemDefinition;

    @BeforeEach
    public void prepareItemDefinitions() {
        itemDefinition = modelInstance.newInstance(ItemDefinition.class);
        definitions.addChildElement(itemDefinition);
    }
}
