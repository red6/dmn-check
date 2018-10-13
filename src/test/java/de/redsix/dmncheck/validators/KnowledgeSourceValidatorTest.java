package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.util.WithDecisionTable;
import org.camunda.bpm.model.dmn.instance.KnowledgeSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KnowledgeSourceValidatorTest extends WithDecisionTable {
    
    private final KnowledgeSourceValidator testee = new KnowledgeSourceValidator();

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

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.get(0);
        assertAll(
                () -> assertEquals("A knowledge source has no id.", validationResult.getMessage()),
                () -> assertEquals(knowledgeSource, validationResult.getElement()),
                () -> assertEquals(Severity.ERROR, validationResult.getSeverity())
        );
    }

    @Test
    void shouldWarnIfKnowledgeSourceHasNoName() {
        knowledgeSource.setId("1");
        knowledgeSource.setName(null);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.get(0);
        assertAll(
                () -> assertEquals("A knowledge source has no name.", validationResult.getMessage()),
                () -> assertEquals(knowledgeSource, validationResult.getElement()),
                () -> assertEquals(Severity.WARNING, validationResult.getSeverity())
        );
    }

    @Test
    void shouldAllowKnowledgeSourceWithIdAndname() {
        knowledgeSource.setId("1");
        knowledgeSource.setName("Test");

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertTrue(validationResults.isEmpty());
    }
}
