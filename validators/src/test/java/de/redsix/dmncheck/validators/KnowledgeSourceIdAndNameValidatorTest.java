package de.redsix.dmncheck.validators;

import static org.junit.jupiter.api.Assertions.*;

import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.util.WithDecisionTable;
import java.util.List;
import org.camunda.bpm.model.dmn.instance.KnowledgeSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class KnowledgeSourceIdAndNameValidatorTest extends WithDecisionTable {

    private final KnowledgeSourceIdAndNameValidator testee =
        new KnowledgeSourceIdAndNameValidator();

    private KnowledgeSource knowledgeSource;

    @BeforeEach
    void addKnowledgeSource() {
        knowledgeSource = modelInstance.newInstance(KnowledgeSource.class);
        definitions.addChildElement(knowledgeSource);
    }

    @Test
    void shouldErrorIfKnowledgeSourceHasNoId() {
        knowledgeSource.setId(null);
        knowledgeSource.setName("Test");

        final List<ValidationResult> validationResults = testee.apply(
            modelInstance
        );

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.getFirst();
        assertAll(
            () ->
                assertEquals(
                    "A knowledge source has no id.",
                    validationResult.getMessage()
                ),
            () -> assertEquals(knowledgeSource, validationResult.getElement()),
            () -> assertEquals(Severity.ERROR, validationResult.getSeverity())
        );
    }

    @Test
    void shouldWarnIfKnowledgeSourceHasNoName() {
        knowledgeSource.setId("1");
        knowledgeSource.setName(null);

        final List<ValidationResult> validationResults = testee.apply(
            modelInstance
        );

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.getFirst();
        assertAll(
            () ->
                assertEquals(
                    "A knowledge source has no name.",
                    validationResult.getMessage()
                ),
            () -> assertEquals(knowledgeSource, validationResult.getElement()),
            () -> assertEquals(Severity.WARNING, validationResult.getSeverity())
        );
    }

    @Test
    void shouldAllowKnowledgeSourceWithIdAndName() {
        knowledgeSource.setId("1");
        knowledgeSource.setName("Test");

        final List<ValidationResult> validationResults = testee.apply(
            modelInstance
        );

        assertTrue(validationResults.isEmpty());
    }
}
