package de.redsix.dmncheck.validators.util;

import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.instance.Decision;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.Definitions;
import org.camunda.bpm.model.dmn.instance.KnowledgeSource;
import org.junit.jupiter.api.BeforeEach;

public class WithDecisionTable {
    protected DmnModelInstance modelInstance;
    protected Definitions definitions;
    protected Decision decision;
    protected DecisionTable decisionTable;
    protected KnowledgeSource knowledgeSource;

    @BeforeEach
    public void prepareDecisionTable() {
        modelInstance = Dmn.createEmptyModel();

        definitions = modelInstance.newInstance(Definitions.class);
        modelInstance.setDefinitions(definitions);

        decision = modelInstance.newInstance(Decision.class);
        definitions.addChildElement(decision);

        decisionTable = modelInstance.newInstance(DecisionTable.class);
        decision.addChildElement(decisionTable);

        knowledgeSource = modelInstance.newInstance(KnowledgeSource.class);
        definitions.addChildElement(knowledgeSource);
    }
}
