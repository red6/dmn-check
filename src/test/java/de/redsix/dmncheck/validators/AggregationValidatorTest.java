package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.result.ValidationResultType;
import org.camunda.bpm.model.dmn.BuiltinAggregator;
import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.HitPolicy;
import org.camunda.bpm.model.dmn.instance.Decision;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.Definitions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AggregationValidatorTest {
    private DmnModelInstance modelInstance;
    private Definitions definitions;
    private Decision decision;
    private DecisionTable decisionTable;

    @BeforeEach
    public void prepareDecisionTable() {
        modelInstance = Dmn.createEmptyModel();
        definitions = modelInstance.newInstance(Definitions.class);
        modelInstance.setDefinitions(definitions);
        decision = modelInstance.newInstance(Decision.class);
        definitions.addChildElement(decision);
        decisionTable = modelInstance.newInstance(DecisionTable.class);
        decision.addChildElement(decisionTable);
    }

    @Test
    public void shouldErrorOnHitPolicyUniqueWithAggregatorCount() {
        decisionTable.setHitPolicy(HitPolicy.UNIQUE);
        decisionTable.setAggregation(BuiltinAggregator.COUNT);

        final List<ValidationResult> validationResults = AggregationValidator.instance.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.get(0);
        assertAll(
                () -> assertEquals("Aggregations are only valid with HitPolicy COLLECT", validationResult.getMessage()),
                () -> assertEquals(decisionTable, validationResult.getElement()),
                () -> assertEquals(ValidationResultType.ERROR, validationResult.getValidationResultType())
        );
    }

    @Test
    public void shouldAllowAggregatorCountWithHitPolicyCollect() {
        decisionTable.setHitPolicy(HitPolicy.COLLECT);
        decisionTable.setAggregation(BuiltinAggregator.COUNT);

        final List<ValidationResult> validationResults = AggregationValidator.instance.apply(modelInstance);

        assertTrue(validationResults.isEmpty());
    }
}
