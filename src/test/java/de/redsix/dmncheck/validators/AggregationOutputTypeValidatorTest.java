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
import org.camunda.bpm.model.dmn.instance.Output;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AggregationOutputTypeValidatorTest {
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
    public void shouldErrorOnStringOutputWithMaxAggregator() {
        decisionTable.setHitPolicy(HitPolicy.COLLECT);
        decisionTable.setAggregation(BuiltinAggregator.MAX);
        final Output output = modelInstance.newInstance(Output.class);
        output.setTypeRef("string");
        decisionTable.getOutputs().add(output);

        final List<ValidationResult> validationResults = AggregationOutputTypeValidator.instance.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.get(0);
        assertAll(
                () -> assertEquals("Aggregations MAX, MIN, SUM are only valid with numeric output types", validationResult.getMessage()),
                () -> assertEquals(output, validationResult.getElement()),
                () -> assertEquals(ValidationResultType.ERROR, validationResult.getValidationResultType())
        );
    }

    @Test
    public void shouldAllowAggregatorMaxWithIntegerOutputs() {
        decisionTable.setHitPolicy(HitPolicy.COLLECT);
        decisionTable.setAggregation(BuiltinAggregator.MAX);
        final Output output = modelInstance.newInstance(Output.class);
        output.setTypeRef("integer");
        decisionTable.getOutputs().add(output);

        final List<ValidationResult> validationResults = AggregationOutputTypeValidator.instance.apply(modelInstance);

        assertTrue(validationResults.isEmpty());
    }
}
