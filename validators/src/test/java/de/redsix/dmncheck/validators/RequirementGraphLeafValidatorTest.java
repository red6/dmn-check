package de.redsix.dmncheck.validators;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.util.WithRequirementGraph;
import java.util.List;
import org.camunda.bpm.model.dmn.instance.Decision;
import org.junit.jupiter.api.Test;

class RequirementGraphLeafValidatorTest extends WithRequirementGraph {

    private final RequirementGraphLeafValidator testee =
        new RequirementGraphLeafValidator();

    @Test
    void shouldAcceptEmptyGraph() {
        final List<ValidationResult> validationResults = testee.apply(
            modelInstance
        );

        assertEquals(0, validationResults.size());
    }

    @Test
    void shouldAcceptGraphWithSingleDecision() {
        final Decision decision = modelInstance.newInstance(Decision.class);
        definitions.addChildElement(decision);

        final List<ValidationResult> validationResults = testee.apply(
            modelInstance
        );

        assertEquals(0, validationResults.size());
    }

    @Test
    void shouldAcceptGraphWithTwoConnectedDecisions() {
        final Decision decision1 = modelInstance.newInstance(Decision.class);
        definitions.addChildElement(decision1);

        final Decision decision2 = modelInstance.newInstance(Decision.class);
        definitions.addChildElement(decision2);

        connect(decision1, decision2);

        final List<ValidationResult> validationResults = testee.apply(
            modelInstance
        );

        assertEquals(0, validationResults.size());
    }

    @Test
    void shouldAcceptGraphWithThreeConnectedDecisionsWithOneLeaf() {
        final Decision decision1 = modelInstance.newInstance(Decision.class);
        definitions.addChildElement(decision1);

        final Decision decision2 = modelInstance.newInstance(Decision.class);
        definitions.addChildElement(decision2);

        final Decision decision3 = modelInstance.newInstance(Decision.class);
        definitions.addChildElement(decision3);

        connect(decision1, decision3);
        connect(decision2, decision3);

        final List<ValidationResult> validationResults = testee.apply(
            modelInstance
        );

        assertEquals(0, validationResults.size());
    }

    @Test
    void shouldRejectGraphWithThreeConnectedDecisionsWithTwoLeafs() {
        final Decision decision1 = modelInstance.newInstance(Decision.class);
        definitions.addChildElement(decision1);

        final Decision decision2 = modelInstance.newInstance(Decision.class);
        definitions.addChildElement(decision2);

        final Decision decision3 = modelInstance.newInstance(Decision.class);
        definitions.addChildElement(decision3);

        connect(decision1, decision2);
        connect(decision1, decision3);

        final List<ValidationResult> validationResults = testee.apply(
            modelInstance
        );

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.getFirst();
        assertAll(
            () ->
                assertEquals(
                    "Requirement graphs may only contain one leaf node",
                    validationResult.getMessage()
                ),
            () -> assertEquals(definitions, validationResult.getElement()),
            () -> assertEquals(Severity.ERROR, validationResult.getSeverity())
        );
    }
}
