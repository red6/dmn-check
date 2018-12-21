package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.util.WithDecisionTable;
import org.camunda.bpm.model.dmn.instance.Input;
import org.camunda.bpm.model.dmn.instance.InputExpression;
import org.camunda.bpm.model.dmn.instance.InputValues;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InputValuesTypeValidatorTest extends WithDecisionTable {

    private final InputValuesTypeValidator testee = new InputValuesTypeValidator();

    @Test
    void shouldAcceptInputWithoutInputValues() {
        final Input input = modelInstance.newInstance(Input.class);

        final InputExpression inputExpression = modelInstance.newInstance(InputExpression.class);
        input.setInputExpression(inputExpression);
        inputExpression.setTypeRef("string");

        decisionTable.getInputs().add(input);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(0, validationResults.size());
    }

    @Test
    void shouldAcceptInputValuesWithCorrectType() {
        final Input input = modelInstance.newInstance(Input.class);
        final InputValues inputValues = modelInstance.newInstance(InputValues.class);
        inputValues.setTextContent("\"foo\",\"bar\"");
        input.setInputValues(inputValues);

        final InputExpression inputExpression = modelInstance.newInstance(InputExpression.class);
        input.setInputExpression(inputExpression);
        inputExpression.setTypeRef("string");

        decisionTable.getInputs().add(input);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(0, validationResults.size());
    }

    @Test
    void shouldDetectThatInputValuesHaveWrongType() {
        final Input input = modelInstance.newInstance(Input.class);
        final InputValues inputValues = modelInstance.newInstance(InputValues.class);
        inputValues.setTextContent("1,2,3");
        input.setInputValues(inputValues);

        final InputExpression inputExpression = modelInstance.newInstance(InputExpression.class);
        input.setInputExpression(inputExpression);
        inputExpression.setTypeRef("string");

        decisionTable.getInputs().add(input);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.get(0);
        assertAll(
                () -> assertEquals("Type of predefined input values does not match type of input expression",
                        validationResult.getMessage()),
                () -> assertEquals(input, validationResult.getElement()),
                () -> assertEquals(Severity.ERROR, validationResult.getSeverity())
        );
    }

}