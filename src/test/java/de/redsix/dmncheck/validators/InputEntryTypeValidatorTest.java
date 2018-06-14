package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.util.ProjectClassLoader;
import de.redsix.dmncheck.validators.util.TestEnum;
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
    
    private final InputEntryTypeValidator testee = new InputEntryTypeValidator();

    @Test
    void shouldAcceptWellTypedInputExpression() {
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

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertTrue(validationResults.isEmpty());
    }

    @Test
    void shouldAcceptWellTypedInputExpressionWithoutTypeDeclaration() {
        final Input input = modelInstance.newInstance(Input.class);
        final InputExpression inputExpression = modelInstance.newInstance(InputExpression.class);
        input.setInputExpression(inputExpression);
        decisionTable.getInputs().add(input);

        final Rule rule = modelInstance.newInstance(Rule.class);
        final InputEntry inputEntry = modelInstance.newInstance(InputEntry.class);
        inputEntry.setTextContent("42");
        rule.getInputEntries().add(inputEntry);
        decisionTable.getRules().add(rule);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertTrue(validationResults.isEmpty());
    }

    @Test
    void shouldAcceptEmptyExpression() {
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

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertTrue(validationResults.isEmpty());
    }

    @Test
    void shouldAcceptIntegersAsLongs() {
        final Input input = modelInstance.newInstance(Input.class);
        final InputExpression inputExpression = modelInstance.newInstance(InputExpression.class);
        input.setInputExpression(inputExpression);
        inputExpression.setTypeRef("long");
        decisionTable.getInputs().add(input);

        final Rule rule = modelInstance.newInstance(Rule.class);
        final InputEntry inputEntry = modelInstance.newInstance(InputEntry.class);
        inputEntry.setTextContent("42");
        rule.getInputEntries().add(inputEntry);
        decisionTable.getRules().add(rule);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertTrue(validationResults.isEmpty());
    }

    @Test
    void shouldAcceptIntegersAsDoubles() {
        final Input input = modelInstance.newInstance(Input.class);
        final InputExpression inputExpression = modelInstance.newInstance(InputExpression.class);
        input.setInputExpression(inputExpression);
        inputExpression.setTypeRef("double");
        decisionTable.getInputs().add(input);

        final Rule rule = modelInstance.newInstance(Rule.class);
        final InputEntry inputEntry = modelInstance.newInstance(InputEntry.class);
        inputEntry.setTextContent("42");
        rule.getInputEntries().add(inputEntry);
        decisionTable.getRules().add(rule);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertTrue(validationResults.isEmpty());
    }

    @Test
    void shouldAcceptBoundVariable() {
        final Input input = modelInstance.newInstance(Input.class);
        final InputExpression inputExpression = modelInstance.newInstance(InputExpression.class);
        input.setCamundaInputVariable("x");
        input.setInputExpression(inputExpression);
        inputExpression.setTypeRef("integer");
        decisionTable.getInputs().add(input);

        final Rule rule = modelInstance.newInstance(Rule.class);
        final InputEntry inputEntry = modelInstance.newInstance(InputEntry.class);
        inputEntry.setTextContent("x");
        rule.getInputEntries().add(inputEntry);
        decisionTable.getRules().add(rule);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertTrue(validationResults.isEmpty());
    }

    @Test
    void shouldAcceptValidEnumValues() {
        final Input input = modelInstance.newInstance(Input.class);
        final InputExpression inputExpression = modelInstance.newInstance(InputExpression.class);
        input.setInputExpression(inputExpression);
        inputExpression.setTypeRef(TestEnum.class.getCanonicalName());
        decisionTable.getInputs().add(input);

        final Rule rule = modelInstance.newInstance(Rule.class);
        final InputEntry inputEntry = modelInstance.newInstance(InputEntry.class);
        inputEntry.setTextContent("\"" + TestEnum.some.name() + "\"");
        rule.getInputEntries().add(inputEntry);
        decisionTable.getRules().add(rule);

        ProjectClassLoader.instance.classLoader = Thread.currentThread().getContextClassLoader();

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertTrue(validationResults.isEmpty());
    }

    @Test
    void shouldNotAcceptInvalidEnumValues() {
        final Input input = modelInstance.newInstance(Input.class);
        final InputExpression inputExpression = modelInstance.newInstance(InputExpression.class);
        input.setInputExpression(inputExpression);
        inputExpression.setTypeRef(TestEnum.class.getCanonicalName());
        decisionTable.getInputs().add(input);

        final Rule rule = modelInstance.newInstance(Rule.class);
        final InputEntry inputEntry = modelInstance.newInstance(InputEntry.class);
        inputEntry.setTextContent("\"" + TestEnum.some.name() + "unkown\"");
        rule.getInputEntries().add(inputEntry);
        decisionTable.getRules().add(rule);

        ProjectClassLoader.instance.classLoader = Thread.currentThread().getContextClassLoader();

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.get(0);
        assertAll(
                () -> assertEquals("Value \"" + TestEnum.some.name() + "unkown\" does not belong to " + TestEnum.class.getCanonicalName(), validationResult.getMessage()),
                () -> assertEquals(rule, validationResult.getElement()),
                () -> assertEquals(Severity.ERROR, validationResult.getSeverity())
        );    }

    @Test
    void shouldRejectUnboundVariable() {
        final Input input = modelInstance.newInstance(Input.class);
        final InputExpression inputExpression = modelInstance.newInstance(InputExpression.class);
        input.setCamundaInputVariable("y");
        input.setInputExpression(inputExpression);
        inputExpression.setTypeRef("integer");
        decisionTable.getInputs().add(input);

        final Rule rule = modelInstance.newInstance(Rule.class);
        final InputEntry inputEntry = modelInstance.newInstance(InputEntry.class);
        inputEntry.setTextContent("x");
        rule.getInputEntries().add(inputEntry);
        decisionTable.getRules().add(rule);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.get(0);
        assertAll(
                () -> assertEquals("Variable 'x' has no type.", validationResult.getMessage()),
                () -> assertEquals(rule, validationResult.getElement()),
                () -> assertEquals(Severity.ERROR, validationResult.getSeverity())
        );
    }

    @Test
    void shouldRejectIllTypedInputExpression() {
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

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.get(0);
        assertAll(
                () -> assertEquals("Type of input entry does not match type of input expression", validationResult.getMessage()),
                () -> assertEquals(rule, validationResult.getElement()),
                () -> assertEquals(Severity.ERROR, validationResult.getSeverity())
        );
    }

    @Test
    void shouldRejectIllTypedInputExpressionWithoutTypeDeclaration() {
        final Input input = modelInstance.newInstance(Input.class);
        final InputExpression inputExpression = modelInstance.newInstance(InputExpression.class);
        input.setInputExpression(inputExpression);
        decisionTable.getInputs().add(input);

        final Rule rule = modelInstance.newInstance(Rule.class);
        final InputEntry inputEntry = modelInstance.newInstance(InputEntry.class);
        inputEntry.setTextContent("[1..true]");
        rule.getInputEntries().add(inputEntry);
        decisionTable.getRules().add(rule);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.get(0);
        assertAll(
                () -> assertEquals("Types of lower and upper bound do not match.", validationResult.getMessage()),
                () -> assertEquals(rule, validationResult.getElement()),
                () -> assertEquals(Severity.ERROR, validationResult.getSeverity())
        );
    }
}
