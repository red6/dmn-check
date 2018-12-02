package de.redsix.dmncheck.validators.util;

import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.instance.Decision;
import org.camunda.bpm.model.dmn.instance.Definitions;
import org.camunda.bpm.model.dmn.instance.DrgElement;
import org.camunda.bpm.model.dmn.instance.InformationRequirement;
import org.junit.jupiter.api.BeforeEach;

public class WithRequirementGraph {
    protected DmnModelInstance modelInstance;
    protected Definitions definitions;

    @BeforeEach
    void prepareRequirementGraph() {
        modelInstance = Dmn.createEmptyModel();

        definitions = modelInstance.newInstance(Definitions.class);
        modelInstance.setDefinitions(definitions);
    }

    protected void connect(Decision decision1, Decision decision2) {
        final InformationRequirement informationRequirement = modelInstance.newInstance(InformationRequirement.class);
        informationRequirement.setRequiredDecision(decision1);
        decision2.addChildElement(informationRequirement);
    }
}
