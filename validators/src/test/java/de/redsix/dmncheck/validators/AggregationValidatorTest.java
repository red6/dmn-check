package de.redsix.dmncheck.validators;

import static org.junit.jupiter.api.Assertions.*;

import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.util.WithDecisionTable;
import java.util.List;
import org.camunda.bpm.model.dmn.BuiltinAggregator;
import org.camunda.bpm.model.dmn.HitPolicy;
import org.junit.jupiter.api.Test;

class AggregationValidatorTest extends WithDecisionTable {

    private final AggregationValidator testee = new AggregationValidator();

    @Test
    void shouldErrorOnHitPolicyUniqueWithAggregatorCount() {
        decisionTable.setHitPolicy(HitPolicy.UNIQUE);
        decisionTable.setAggregation(BuiltinAggregator.COUNT);

        final List<ValidationResult> validationResults = testee.apply(
            modelInstance
        );

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.getFirst();
        assertAll(
            () ->
                assertEquals(
                    "Aggregations are only valid with HitPolicy COLLECT",
                    validationResult.getMessage()
                ),
            () -> assertEquals(decisionTable, validationResult.getElement()),
            () -> assertEquals(Severity.ERROR, validationResult.getSeverity())
        );
    }

    @Test
    void shouldAllowAggregatorCountWithHitPolicyCollect() {
        decisionTable.setHitPolicy(HitPolicy.COLLECT);
        decisionTable.setAggregation(BuiltinAggregator.COUNT);

        final List<ValidationResult> validationResults = testee.apply(
            modelInstance
        );

        assertTrue(validationResults.isEmpty());
    }

    @Test
    void shouldSkipDecisionTablesWithoutAggregators() {
        decisionTable.setHitPolicy(HitPolicy.UNIQUE);
        assertNull(decisionTable.getAggregation());

        final List<ValidationResult> validationResults = testee.apply(
            modelInstance
        );

        assertTrue(validationResults.isEmpty());
    }
}
