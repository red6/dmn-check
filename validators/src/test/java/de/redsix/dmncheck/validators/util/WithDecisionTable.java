package de.redsix.dmncheck.validators.util;

import org.camunda.bpm.model.dmn.instance.Decision;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.junit.jupiter.api.BeforeEach;

public class WithDecisionTable extends WithDefinitions {
    protected Decision decision;
    protected DecisionTable decisionTable;

    @BeforeEach
    public void prepareDecisionTable() {
        decision = modelInstance.newInstance(Decision.class);
        definitions.addChildElement(decision);

        decisionTable = modelInstance.newInstance(DecisionTable.class);
        decision.addChildElement(decisionTable);
    }
}
