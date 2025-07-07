package de.redsix.dmncheck.validators;

import static org.junit.jupiter.api.Assertions.*;

import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.util.WithDecisionTable;
import java.util.List;
import org.camunda.bpm.model.dmn.instance.Input;
import org.camunda.bpm.model.dmn.instance.InputExpression;
import org.camunda.bpm.model.dmn.instance.InputValues;
import org.junit.jupiter.api.Test;

class InputValuesTypeValidatorTest extends WithDecisionTable {

    private final InputValuesTypeValidator testee =
        new InputValuesTypeValidator();

    @Test
    void shouldAcceptInputWithoutInputValues() {
        final Input input = modelInstance.newInstance(Input.class);

        final InputExpression inputExpression = modelInstance.newInstance(
            InputExpression.class
        );
        input.setInputExpression(inputExpression);
        inputExpression.setTypeRef("string");

        decisionTable.getInputs().add(input);

        final List<ValidationResult> validationResults = testee.apply(
            modelInstance
        );

        assertEquals(0, validationResults.size());
    }

    @Test
    void shouldAcceptInputValuesWithCorrectType() {
        final Input input = modelInstance.newInstance(Input.class);
        final InputValues inputValues = modelInstance.newInstance(
            InputValues.class
        );
        inputValues.setTextContent("\"foo\",\"bar\"");
        input.setInputValues(inputValues);

        final InputExpression inputExpression = modelInstance.newInstance(
            InputExpression.class
        );
        input.setInputExpression(inputExpression);
        inputExpression.setTypeRef("string");

        decisionTable.getInputs().add(input);

        final List<ValidationResult> validationResults = testee.apply(
            modelInstance
        );

        assertEquals(0, validationResults.size());
    }

    @Test
    void shouldDetectThatInputValuesHaveWrongType() {
        final Input input = modelInstance.newInstance(Input.class);
        final InputValues inputValues = modelInstance.newInstance(
            InputValues.class
        );
        inputValues.setTextContent("1,2,3");
        input.setInputValues(inputValues);

        final InputExpression inputExpression = modelInstance.newInstance(
            InputExpression.class
        );
        input.setInputExpression(inputExpression);
        inputExpression.setTypeRef("string");

        decisionTable.getInputs().add(input);

        final List<ValidationResult> validationResults = testee.apply(
            modelInstance
        );

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.getFirst();
        assertAll(
            () ->
                assertEquals(
                    "Type of predefined input values does not match type of input expression",
                    validationResult.getMessage()
                ),
            () -> assertEquals(input, validationResult.getElement()),
            () -> assertEquals(Severity.ERROR, validationResult.getSeverity())
        );
    }

    @Test
    void warnsIfAnOtherExpressionLanguageThanFeelIsUsed() {
        final Input input = modelInstance.newInstance(Input.class);
        final InputValues inputValues = modelInstance.newInstance(
            InputValues.class
        );
        inputValues.setTextContent("'foo'.repeat(6)");
        inputValues.setExpressionLanguage("javascript");
        input.setInputValues(inputValues);

        final InputExpression inputExpression = modelInstance.newInstance(
            InputExpression.class
        );
        input.setInputExpression(inputExpression);
        inputExpression.setTypeRef("string");

        decisionTable.getInputs().add(input);

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
            () -> assertEquals(input, validationResult.getElement()),
            () -> assertEquals(Severity.WARNING, validationResult.getSeverity())
        );
    }
}
