package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.result.ValidationResultType;
import de.redsix.dmncheck.validators.util.WithDecisionTable;
import org.camunda.bpm.model.dmn.instance.Input;
import org.camunda.bpm.model.dmn.instance.InputEntry;
import org.camunda.bpm.model.dmn.instance.InputExpression;
import org.camunda.bpm.model.dmn.instance.Rule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InputEntryTypeValidatorTest extends WithDecisionTable {

    @Test
    public void shouldAcceptWellTypedInputExpression() {
            final Input input = modelInstance.newInstance(Input.class);
            final InputExpression inputExpression = modelInstance.newInstance(InputExpression.class);
            input.setInputExpression(inputExpression);
            inputExpression.setTypeRef("integer");
            decisionTable.getInputs().add(input);

            final Rule rule = modelInstance.newInstance(Rule.class);
            final InputEntry inputEntry = modelInstance.newInstance(InputEntry.class);
            inputEntry.setTextContent("42");
            rule.getInputEntries().add(inputEntry);
            decisionTable.getRules().add(rule);

            final List<ValidationResult> validationResults = InputEntryTypeValidator.instance.apply(modelInstance);

            assertTrue(validationResults.isEmpty());
    }

    @Test
    public void shouldAcceptEmptyExpression() {
        final Input input = modelInstance.newInstance(Input.class);
        final InputExpression inputExpression = modelInstance.newInstance(InputExpression.class);
        input.setInputExpression(inputExpression);
        inputExpression.setTypeRef("integer");
        decisionTable.getInputs().add(input);

        final Rule rule = modelInstance.newInstance(Rule.class);
        final InputEntry inputEntry = modelInstance.newInstance(InputEntry.class);
        inputEntry.setTextContent(null);
        rule.getInputEntries().add(inputEntry);
        decisionTable.getRules().add(rule);

        final List<ValidationResult> validationResults = InputEntryTypeValidator.instance.apply(modelInstance);

        assertTrue(validationResults.isEmpty());
    }

    @Test
    public void shouldRejectIllTypedInputExpression() {
        final Input input = modelInstance.newInstance(Input.class);
        final InputExpression inputExpression = modelInstance.newInstance(InputExpression.class);
        input.setInputExpression(inputExpression);
        inputExpression.setTypeRef("integer");
        decisionTable.getInputs().add(input);

        final Rule rule = modelInstance.newInstance(Rule.class);
        final InputEntry inputEntry = modelInstance.newInstance(InputEntry.class);
        inputEntry.setTextContent("\"Steak\"");
        rule.getInputEntries().add(inputEntry);
        decisionTable.getRules().add(rule);

        final List<ValidationResult> validationResults = InputEntryTypeValidator.instance.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.get(0);
        assertAll(
                () -> assertEquals("Type of input entry does not match type of input expression", validationResult.getMessage()),
                () -> assertEquals(rule, validationResult.getElement()),
                () -> assertEquals(ValidationResultType.ERROR, validationResult.getValidationResultType())
        );
    }
}
