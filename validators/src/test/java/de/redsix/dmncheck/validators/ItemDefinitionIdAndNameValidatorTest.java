package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.util.WithItemDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemDefinitionIdAndNameValidatorTest extends WithItemDefinition {

    private final ItemDefinitionIdAndNameValidator testee = new ItemDefinitionIdAndNameValidator();

    @Test
    void shouldErrorIfDecisionHasNoId() {
        itemDefinition.setId(null);
        itemDefinition.setName("Test");

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.get(0);
        assertAll(
                () -> assertEquals("A item definition has no id.", validationResult.getMessage()),
                () -> assertEquals(itemDefinition, validationResult.getElement()),
                () -> assertEquals(Severity.ERROR, validationResult.getSeverity())
        );
    }

    @Test
    void shouldWarnIfDecisionHasNoName() {
        itemDefinition.setId("1");
        itemDefinition.setName(null);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.get(0);
        assertAll(
                () -> assertEquals("A item definition has no name.", validationResult.getMessage()),
                () -> assertEquals(itemDefinition, validationResult.getElement()),
                () -> assertEquals(Severity.WARNING, validationResult.getSeverity())
        );
    }

    @Test
    void shouldAllowItemDefinitionWithIdAndName() {
        itemDefinition.setId("1");
        itemDefinition.setName("Test");

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertTrue(validationResults.isEmpty());
    }

}
