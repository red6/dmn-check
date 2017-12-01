package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.result.ValidationResultType;
import de.redsix.dmncheck.validators.util.WithDecisionTable;
import org.camunda.bpm.model.dmn.instance.Output;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OutputTypeValidatorTest extends WithDecisionTable {

    @Test
    public void shouldDetectThatOutputHasNoType() {
        final Output output = modelInstance.newInstance(Output.class);
        decisionTable.getOutputs().add(output);

        final List<ValidationResult> validationResults = OutputTypeValidator.instance.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.get(0);
        assertAll(
                () -> assertEquals("Output has no type", validationResult.getMessage()),
                () -> assertEquals(output, validationResult.getElement()),
                () -> assertEquals(ValidationResultType.ERROR, validationResult.getValidationResultType())
        );
    }

    @Test
    public void shouldDetectThatOutputHasUnsupportedType() {
        final Output output = modelInstance.newInstance(Output.class);
        output.setTypeRef("unsupportedType");
        decisionTable.getOutputs().add(output);

        final List<ValidationResult> validationResults = OutputTypeValidator.instance.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.get(0);
        assertAll(
                () -> assertEquals("Output uses an unsupported type", validationResult.getMessage()),
                () -> assertEquals(output, validationResult.getElement()),
                () -> assertEquals(ValidationResultType.ERROR, validationResult.getValidationResultType())
        );
    }

    @Test
    public void shouldAllowOutputWithSupportedType() {
        final Output output = modelInstance.newInstance(Output.class);
        output.setTypeRef("integer");
        decisionTable.getOutputs().add(output);

        final List<ValidationResult> validationResults = OutputTypeValidator.instance.apply(modelInstance);

        assertTrue(validationResults.isEmpty());
    }
}
