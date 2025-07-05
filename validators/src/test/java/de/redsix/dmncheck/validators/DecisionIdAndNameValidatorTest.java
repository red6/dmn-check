package de.redsix.dmncheck.validators;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.util.WithDecisionTable;
import java.util.List;
import org.junit.jupiter.api.Test;

class DecisionIdAndNameValidatorTest extends WithDecisionTable {

    private final DecisionIdAndNameValidator testee = new DecisionIdAndNameValidator();

    @Test
    void shouldErrorIfDecisionHasNoId() {
        decision.setId(null);
        decision.setName("Test");

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.getFirst();
        assertAll(
                () -> assertEquals("A decision has no id.", validationResult.getMessage()),
                () -> assertEquals(decision, validationResult.getElement()),
                () -> assertEquals(Severity.ERROR, validationResult.getSeverity()));
    }

    @Test
    void shouldWarnIfDecisionHasNoName() {
        decision.setId("1");
        decision.setName(null);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.getFirst();
        assertAll(
                () -> assertEquals("A decision has no name.", validationResult.getMessage()),
                () -> assertEquals(decision, validationResult.getElement()),
                () -> assertEquals(Severity.WARNING, validationResult.getSeverity()));
    }

    @Test
    void shouldAllowAggregatorCountWithHitPolicyCollect() {
        decision.setId("1");
        decision.setName("Test");

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertTrue(validationResults.isEmpty());
    }
}
