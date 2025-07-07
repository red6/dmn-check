package de.redsix.dmncheck.validators;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.util.WithDecisionTable;
import java.util.List;
import org.camunda.bpm.model.dmn.instance.Output;
import org.junit.jupiter.api.Test;

class OutputTypeDeclarationValidatorTest extends WithDecisionTable {

    private final OutputTypeDeclarationValidator testee =
        new OutputTypeDeclarationValidator();

    @Test
    void shouldDetectThatOutputHasNoType() {
        final Output output = modelInstance.newInstance(Output.class);
        decisionTable.getOutputs().add(output);

        final List<ValidationResult> validationResults = testee.apply(
            modelInstance
        );

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.getFirst();
        assertAll(
            () ->
                assertEquals(
                    "Output has no type",
                    validationResult.getMessage()
                ),
            () -> assertEquals(output, validationResult.getElement()),
            () -> assertEquals(Severity.WARNING, validationResult.getSeverity())
        );
    }

    @Test
    void shouldDetectThatOutputHasUnsupportedType() {
        final Output output = modelInstance.newInstance(Output.class);
        output.setTypeRef("unsupportedType");
        decisionTable.getOutputs().add(output);

        final List<ValidationResult> validationResults = testee.apply(
            modelInstance
        );

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.getFirst();
        assertAll(
            () ->
                assertEquals(
                    "Could not parse FEEL expression type 'unsupportedType'",
                    validationResult.getMessage()
                ),
            () -> assertEquals(output, validationResult.getElement()),
            () -> assertEquals(Severity.ERROR, validationResult.getSeverity())
        );
    }

    @Test
    void shouldAllowOutputWithSupportedType() {
        final Output output = modelInstance.newInstance(Output.class);
        output.setTypeRef("integer");
        decisionTable.getOutputs().add(output);

        final List<ValidationResult> validationResults = testee.apply(
            modelInstance
        );

        assertTrue(validationResults.isEmpty());
    }
}
