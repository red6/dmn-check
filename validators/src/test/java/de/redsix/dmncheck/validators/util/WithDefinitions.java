package de.redsix.dmncheck.validators.util;

import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.instance.Definitions;
import org.junit.jupiter.api.BeforeEach;

public class WithDefinitions {

    protected DmnModelInstance modelInstance;
    protected Definitions definitions;

    @BeforeEach
    public void prepareDefinitions() {
        modelInstance = Dmn.createEmptyModel();

        definitions = modelInstance.newInstance(Definitions.class);
        modelInstance.setDefinitions(definitions);
    }
}
