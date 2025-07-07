package de.redsix.dmncheck.validators;

import static org.junit.jupiter.api.Assertions.*;

import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.util.WithDecisionTable;
import java.util.List;
import org.camunda.bpm.model.dmn.instance.Output;
import org.camunda.bpm.model.dmn.instance.OutputValues;
import org.junit.jupiter.api.Test;

class OutputValuesTypeValidatorTest extends WithDecisionTable {

    private final OutputValuesTypeValidator testee =
        new OutputValuesTypeValidator();

    @Test
    void shouldAcceptOutputWithoutOutputValues() {
        final Output output = modelInstance.newInstance(Output.class);
        output.setTypeRef("string");

        decisionTable.getOutputs().add(output);

        final List<ValidationResult> validationResults = testee.apply(
            modelInstance
        );

        assertEquals(0, validationResults.size());
    }

    @Test
    void shouldAcceptOutputValuesWithCorrectType() {
        final Output output = modelInstance.newInstance(Output.class);
        final OutputValues OutputValues = modelInstance.newInstance(
            OutputValues.class
        );
        OutputValues.setTextContent("\"foo\",\"bar\"");
        output.setOutputValues(OutputValues);
        output.setTypeRef("string");

        decisionTable.getOutputs().add(output);

        final List<ValidationResult> validationResults = testee.apply(
            modelInstance
        );

        assertEquals(0, validationResults.size());
    }

    @Test
    void shouldDetectThatOutputValuesHaveWrongType() {
        final Output output = modelInstance.newInstance(Output.class);
        final OutputValues OutputValues = modelInstance.newInstance(
            OutputValues.class
        );
        OutputValues.setTextContent("1,2,3");
        output.setOutputValues(OutputValues);
        output.setTypeRef("string");

        decisionTable.getOutputs().add(output);

        final List<ValidationResult> validationResults = testee.apply(
            modelInstance
        );

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.getFirst();
        assertAll(
            () ->
                assertEquals(
                    "Type of predefined output values does not match type of output expression",
                    validationResult.getMessage()
                ),
            () -> assertEquals(output, validationResult.getElement()),
            () -> assertEquals(Severity.ERROR, validationResult.getSeverity())
        );
    }

    @Test
    void warnsIfAnOtherExpressionLanguageThanFeelIsUsed() {
        final Output output = modelInstance.newInstance(Output.class);
        final OutputValues outputValues = modelInstance.newInstance(
            OutputValues.class
        );
        outputValues.setTextContent("'foo'.repeat(6)");
        outputValues.setExpressionLanguage("javascript");
        output.setOutputValues(outputValues);
        output.setTypeRef("string");

        decisionTable.getOutputs().add(output);

        final List<ValidationResult> validationResults = testee.apply(
            modelInstance
        );

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.getFirst();
        assertAll(
            () ->
                assertEquals(
                    "Expression language 'javascript' not supported",
                    validationResult.getMessage()
                ),
            () -> assertEquals(output, validationResult.getElement()),
            () -> assertEquals(Severity.WARNING, validationResult.getSeverity())
        );
    }
}
