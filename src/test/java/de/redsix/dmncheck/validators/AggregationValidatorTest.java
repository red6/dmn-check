package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.result.ValidationResultType;
import de.redsix.dmncheck.validators.util.WithDecisionTable;
import org.camunda.bpm.model.dmn.BuiltinAggregator;
import org.camunda.bpm.model.dmn.HitPolicy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AggregationValidatorTest extends WithDecisionTable {

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
