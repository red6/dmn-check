package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.result.ValidationResultType;
import de.redsix.dmncheck.validators.util.WithDecisionTable;
import org.camunda.bpm.model.dmn.instance.Output;
import org.camunda.bpm.model.dmn.instance.OutputEntry;
import org.camunda.bpm.model.dmn.instance.Rule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OutputEntryValidatorTest extends WithDecisionTable {

    @Test
    void shouldAcceptWellTypedInputExpression() {
        final Output output = modelInstance.newInstance(Output.class);
        output.setTypeRef("integer");
        decisionTable.getOutputs().add(output);

        final Rule rule = modelInstance.newInstance(Rule.class);
        final OutputEntry outputEntry = modelInstance.newInstance(OutputEntry.class);
        outputEntry.setTextContent("42");
        rule.getOutputEntries().add(outputEntry);
        decisionTable.getRules().add(rule);

        final List<ValidationResult> validationResults = OutputEntryTypeValidator.instance.apply(modelInstance);

        assertTrue(validationResults.isEmpty());
    }

    @Test
    void shouldAcceptWellTypedInputExpressionWithoutTypeDeclaration() {
        final Output output = modelInstance.newInstance(Output.class);
        decisionTable.getOutputs().add(output);

        final Rule rule = modelInstance.newInstance(Rule.class);
        final OutputEntry outputEntry = modelInstance.newInstance(OutputEntry.class);
        outputEntry.setTextContent("42");
        rule.getOutputEntries().add(outputEntry);
        decisionTable.getRules().add(rule);

        final List<ValidationResult> validationResults = OutputEntryTypeValidator.instance.apply(modelInstance);

        assertTrue(validationResults.isEmpty());
    }

    @Test
    void shouldAcceptEmptyExpression() {
        final Output output = modelInstance.newInstance(Output.class);
        output.setTypeRef("integer");
        decisionTable.getOutputs().add(output);

        final Rule rule = modelInstance.newInstance(Rule.class);
        final OutputEntry outputEntry = modelInstance.newInstance(OutputEntry.class);
        outputEntry.setTextContent(null);
        rule.getOutputEntries().add(outputEntry);
        decisionTable.getRules().add(rule);

        final List<ValidationResult> validationResults = OutputEntryTypeValidator.instance.apply(modelInstance);

        assertFalse(validationResults.isEmpty());
    }

    @Test
    void shouldRejectIllTypedInputExpression() {
        final Output output = modelInstance.newInstance(Output.class);
        output.setTypeRef("integer");
        decisionTable.getOutputs().add(output);

        final Rule rule = modelInstance.newInstance(Rule.class);
        final OutputEntry outputEntry = modelInstance.newInstance(OutputEntry.class);
        outputEntry.setTextContent("\"Steak\"");
        rule.getOutputEntries().add(outputEntry);
        decisionTable.getRules().add(rule);

        final List<ValidationResult> validationResults = OutputEntryTypeValidator.instance.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.get(0);
        assertAll(
                () -> assertEquals("Type of output entry does not match type of output expression", validationResult.getMessage()),
                () -> assertEquals(rule, validationResult.getElement()),
                () -> assertEquals(ValidationResultType.ERROR, validationResult.getValidationResultType())
        );
    }

    @Test
    void shouldRejectIllTypedInputExpressionWithoutTypeDeclaration() {
        final Output output = modelInstance.newInstance(Output.class);
        decisionTable.getOutputs().add(output);

        final Rule rule = modelInstance.newInstance(Rule.class);
        final OutputEntry outputEntry = modelInstance.newInstance(OutputEntry.class);
        outputEntry.setTextContent("[1..true]");
        rule.getOutputEntries().add(outputEntry);
        decisionTable.getRules().add(rule);

        final List<ValidationResult> validationResults = OutputEntryTypeValidator.instance.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.get(0);
        assertAll(
                () -> assertEquals("Types of lower and upper bound do not match.", validationResult.getMessage()),
                () -> assertEquals(rule, validationResult.getElement()),
                () -> assertEquals(ValidationResultType.ERROR, validationResult.getValidationResultType())
        );
    }
}
