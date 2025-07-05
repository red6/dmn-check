package de.redsix.dmncheck.validators;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.util.WithDefinitions;
import java.util.List;
import org.junit.jupiter.api.Test;

class DefinitionsIdAndNameValidatorTest extends WithDefinitions {

    private final DefinitionsIdAndNameValidator testee = new DefinitionsIdAndNameValidator();

    @Test
    void shouldErrorIfDefinitionsHasNoId() {
        definitions.setId(null);
        definitions.setName("Test");

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.getFirst();
        assertAll(
                () -> assertEquals("A definitions has no id.", validationResult.getMessage()),
                () -> assertEquals(definitions, validationResult.getElement()),
                () -> assertEquals(Severity.ERROR, validationResult.getSeverity()));
    }

    @Test
    void shouldWarnIfDefinitionsHasNoName() {
        definitions.setId("1");
        definitions.setName(null);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.getFirst();
        assertAll(
                () -> assertEquals("A definitions has no name.", validationResult.getMessage()),
                () -> assertEquals(definitions, validationResult.getElement()),
                () -> assertEquals(Severity.WARNING, validationResult.getSeverity()));
    }

    @Test
    void shouldAcceptDefinitionsWithIdAndName() {
        definitions.setId("1");
        definitions.setName("Test");

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertTrue(validationResults.isEmpty());
    }
}
