package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.validators.util.WithDecisionTable;
import org.camunda.bpm.model.dmn.instance.Output;
import org.camunda.bpm.model.dmn.instance.OutputEntry;
import org.camunda.bpm.model.dmn.instance.Rule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OutputEntryTypeValidatorTest extends WithDecisionTable {

    private final OutputEntryTypeValidator testee = new OutputEntryTypeValidator();

    @ParameterizedTest
    @CsvSource({"integer, 42", "long, 42", "double, 42", "integer, "})
    void shouldAcceptWellTypedOutputExpression(final String typeref, final String textContent) {
        final Output output = modelInstance.newInstance(Output.class);
        output.setTypeRef(typeref);
        decisionTable.getOutputs().add(output);

        final Rule rule = modelInstance.newInstance(Rule.class);
        final OutputEntry outputEntry = modelInstance.newInstance(OutputEntry.class);
        outputEntry.setTextContent(textContent);
        rule.getOutputEntries().add(outputEntry);
        decisionTable.getRules().add(rule);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertTrue(validationResults.isEmpty());
    }

    @Test
    void shouldAcceptWellTypedOutputExpressionWithoutTypeDeclaration() {
        final Output output = modelInstance.newInstance(Output.class);
        decisionTable.getOutputs().add(output);

        final Rule rule = modelInstance.newInstance(Rule.class);
        final OutputEntry outputEntry = modelInstance.newInstance(OutputEntry.class);
        outputEntry.setTextContent("42");
        rule.getOutputEntries().add(outputEntry);
        decisionTable.getRules().add(rule);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertTrue(validationResults.isEmpty());
    }

    @Test
    void shouldRejectIllTypedOutputExpression() {
        final Output output = modelInstance.newInstance(Output.class);
        output.setTypeRef("integer");
        decisionTable.getOutputs().add(output);

        final Rule rule = modelInstance.newInstance(Rule.class);
        final OutputEntry outputEntry = modelInstance.newInstance(OutputEntry.class);
        outputEntry.setTextContent("\"Steak\"");
        rule.getOutputEntries().add(outputEntry);
        decisionTable.getRules().add(rule);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.get(0);
        assertAll(
                () -> assertEquals("Type of output entry does not match type of output expression", validationResult.getMessage()),
                () -> assertEquals(rule, validationResult.getElement()),
                () -> assertEquals(Severity.ERROR, validationResult.getSeverity())
        );
    }

    @Test
    void shouldRejectIllTypedOutputExpressionWithoutTypeDeclaration() {
        final Output output = modelInstance.newInstance(Output.class);
        decisionTable.getOutputs().add(output);

        final Rule rule = modelInstance.newInstance(Rule.class);
        final OutputEntry outputEntry = modelInstance.newInstance(OutputEntry.class);
        outputEntry.setTextContent("[1..true]");
        rule.getOutputEntries().add(outputEntry);
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
