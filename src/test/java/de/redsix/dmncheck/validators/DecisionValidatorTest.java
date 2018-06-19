package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.util.WithDecisionTable;
import org.camunda.bpm.model.dmn.BuiltinAggregator;
import org.camunda.bpm.model.dmn.HitPolicy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DecisionValidatorTest extends WithDecisionTable {
    
    private final DecisionValidator testee = new DecisionValidator();

    @Test
    void shouldErrorIfDecisionHasNoId() {
        decision.setId(null);
        decision.setName("Test");

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.get(0);
        assertAll(
                () -> assertEquals("A decision has no id.", validationResult.getMessage()),
                () -> assertEquals(decision, validationResult.getElement()),
                () -> assertEquals(Severity.ERROR, validationResult.getSeverity())
        );
    }

    @Test
    void shouldWarnIfDecisionHasNoName() {
        decision.setId("1");
        decision.setName(null);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.get(0);
        assertAll(
                () -> assertEquals("A decision has no name.", validationResult.getMessage()),
                () -> assertEquals(decision, validationResult.getElement()),
                () -> assertEquals(Severity.WARNING, validationResult.getSeverity())
        );
    }

    @Test
    void shouldAllowAggregatorCountWithHitPolicyCollect() {
        decision.setId("1");
        decision.setName("Test");

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertTrue(validationResults.isEmpty());
    }
}
