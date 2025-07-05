package de.redsix.dmncheck.validators;

import static org.junit.jupiter.api.Assertions.*;

import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.util.WithDecisionTable;
import java.util.List;
import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.instance.Definitions;
import org.camunda.bpm.model.dmn.instance.KnowledgeSource;
import org.junit.jupiter.api.Test;

class NoDecisionPresentValidatorTest extends WithDecisionTable {

    private final NoDecisionPresentValidator testee = new NoDecisionPresentValidator();

    @Test
    void shouldDetectThatDefinitionsContainNoDecisions() {
        final DmnModelInstance dmnModelInstance = Dmn.createEmptyModel();

        final Definitions definitionsWithOnlyOneKnowledgeSource = dmnModelInstance.newInstance(Definitions.class);
        dmnModelInstance.setDefinitions(definitionsWithOnlyOneKnowledgeSource);

        final KnowledgeSource knowledgeSource = dmnModelInstance.newInstance(KnowledgeSource.class);
        definitionsWithOnlyOneKnowledgeSource.addChildElement(knowledgeSource);

        final List<ValidationResult> validationResults = testee.apply(dmnModelInstance);

        assertEquals(1, validationResults.size());

        final ValidationResult validationResult = validationResults.getFirst();
        assertAll(
                () -> assertEquals("No decisions found", validationResult.getMessage()),
                () -> assertEquals(definitionsWithOnlyOneKnowledgeSource, validationResult.getElement()),
                () -> assertEquals(Severity.WARNING, validationResult.getSeverity()));
    }

    @Test
    void shouldAcceptModelWithOneDecision() {
        // Decision is defined in super class WithDecisionTable

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(0, validationResults.size());
    }
}
