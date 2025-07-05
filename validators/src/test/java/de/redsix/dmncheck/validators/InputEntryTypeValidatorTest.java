package de.redsix.dmncheck.validators;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.util.ProjectClassLoader;
import de.redsix.dmncheck.validators.util.TestEnum;
import de.redsix.dmncheck.validators.util.WithDecisionTable;
import java.util.List;
import org.camunda.bpm.model.dmn.instance.Input;
import org.camunda.bpm.model.dmn.instance.InputEntry;
import org.camunda.bpm.model.dmn.instance.InputExpression;
import org.camunda.bpm.model.dmn.instance.Rule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class InputEntryTypeValidatorTest extends WithDecisionTable {

    private final InputEntryTypeValidator testee = new InputEntryTypeValidator();

    @ParameterizedTest
    @CsvSource({"integer, 42", "long, 42", "double, 42", "integer, "})
    void shouldAcceptWellTypedInputExpression(final String typeRef, final String textContent) {
        final Input input = modelInstance.newInstance(Input.class);
        final InputExpression inputExpression = modelInstance.newInstance(InputExpression.class);
        input.setInputExpression(inputExpression);
        inputExpression.setTypeRef(typeRef);
        decisionTable.getInputs().add(input);

        final Rule rule = modelInstance.newInstance(Rule.class);
        final InputEntry inputEntry = modelInstance.newInstance(InputEntry.class);
        inputEntry.setTextContent(textContent);
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

        ProjectClassLoader.INSTANCE.classLoader = Thread.currentThread().getContextClassLoader();

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

        ProjectClassLoader.INSTANCE.classLoader = Thread.currentThread().getContextClassLoader();

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.getFirst();
        assertAll(
                () -> assertEquals(
                        "Value \"" + TestEnum.some.name() + "unkown\" does not belong to "
                                + TestEnum.class.getCanonicalName(),
                        validationResult.getMessage()),
                () -> assertEquals(rule, validationResult.getElement()),
                () -> assertEquals(Severity.ERROR, validationResult.getSeverity()));
    }

    @Test
    void shouldFailIfProjectclassloaderIsAbsent() {
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

        ProjectClassLoader.INSTANCE.classLoader = null;

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.getFirst();
        assertAll(
                () -> assertEquals("Classloader of project under validation not found", validationResult.getMessage()),
                () -> assertEquals(rule, validationResult.getElement()),
                () -> assertEquals(Severity.ERROR, validationResult.getSeverity()));
    }

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
        final ValidationResult validationResult = validationResults.getFirst();
        assertAll(
                () -> assertEquals("Variable 'x' has no type.", validationResult.getMessage()),
                () -> assertEquals(rule, validationResult.getElement()),
                () -> assertEquals(Severity.WARNING, validationResult.getSeverity()));
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
        final ValidationResult validationResult = validationResults.getFirst();
        assertAll(
                () -> assertEquals(
                        "Type of input entry does not match type of input expression", validationResult.getMessage()),
                () -> assertEquals(rule, validationResult.getElement()),
                () -> assertEquals(Severity.ERROR, validationResult.getSeverity()));
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
        final ValidationResult validationResult = validationResults.getFirst();
        assertAll(
                () -> assertEquals("Types of lower and upper bound do not match.", validationResult.getMessage()),
                () -> assertEquals(rule, validationResult.getElement()),
                () -> assertEquals(Severity.ERROR, validationResult.getSeverity()));
    }

    @Test
    void warnsIfAnOtherExpressionLanguageThanFeelIsUsed() {
        final Input input = modelInstance.newInstance(Input.class);
        final InputExpression inputExpression = modelInstance.newInstance(InputExpression.class);
        input.setInputExpression(inputExpression);
        decisionTable.getInputs().add(input);

        final Rule rule = modelInstance.newInstance(Rule.class);
        final InputEntry inputEntry = modelInstance.newInstance(InputEntry.class);
        inputEntry.setTextContent("'foo'.repeat(6)");
        inputEntry.setExpressionLanguage("javascript");
        rule.getInputEntries().add(inputEntry);
        decisionTable.getRules().add(rule);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.getFirst();
        assertAll(
                () -> assertEquals("Expression language 'javascript' not supported", validationResult.getMessage()),
                () -> assertEquals(rule, validationResult.getElement()),
                () -> assertEquals(Severity.WARNING, validationResult.getSeverity()));
    }
}
