package de.redsix.dmncheck.validators;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.util.WithDecisionTable;
import java.util.List;
import org.camunda.bpm.model.dmn.instance.Input;
import org.camunda.bpm.model.dmn.instance.InputEntry;
import org.camunda.bpm.model.dmn.instance.Output;
import org.camunda.bpm.model.dmn.instance.OutputEntry;
import org.camunda.bpm.model.dmn.instance.Rule;
import org.junit.jupiter.api.Test;

class DecisionTableValidatorTest extends WithDecisionTable {

    private final DecisionTableValidator testee = new DecisionTableValidator();

    @Test
    void wellFormedDecisionTableIsValid() {
        decisionTable.getInputs().add(modelInstance.newInstance(Input.class));
        decisionTable.getOutputs().add(modelInstance.newInstance(Output.class));
        addRule(1, 1);

        assertEquals(List.of(), testee.apply(modelInstance));
    }

    @Test
    void decisionTableWithoutInputsIsValid() {
        decisionTable.getOutputs().add(modelInstance.newInstance(Output.class));
        addRule(0, 1);

        assertEquals(List.of(), testee.apply(modelInstance));
    }

    @Test
    void shouldDetectMissingOutputs() {
        addRule(0, 0);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.getFirst();
        assertAll(
                () -> assertEquals("Decision table has no outputs", validationResult.getMessage()),
                () -> assertEquals(decisionTable, validationResult.getElement()),
                () -> assertEquals(Severity.ERROR, validationResult.getSeverity()));
    }

    @Test
    void shouldDetectUnnamedOutputsIfThereAreMultipleOutputs() {
        final Output namedOutput = modelInstance.newInstance(Output.class);
        namedOutput.setName("dish");
        final Output unnamedOutput = modelInstance.newInstance(Output.class);
        decisionTable.getOutputs().add(namedOutput);
        decisionTable.getOutputs().add(unnamedOutput);
        addRule(0, 2);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.getFirst();
        assertAll(
                () -> assertEquals(
                        "Output has no name, but decision tables with more than one output require named outputs",
                        validationResult.getMessage()),
                () -> assertEquals(unnamedOutput, validationResult.getElement()),
                () -> assertEquals(Severity.ERROR, validationResult.getSeverity()));
    }

    @Test
    void shouldWarnAboutMissingRules() {
        decisionTable.getOutputs().add(modelInstance.newInstance(Output.class));

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.getFirst();
        assertAll(
                () -> assertEquals("Decision table has no rules", validationResult.getMessage()),
                () -> assertEquals(decisionTable, validationResult.getElement()),
                () -> assertEquals(Severity.WARNING, validationResult.getSeverity()));
    }

    @Test
    void shouldDetectRulesWithWrongNumberOfEntries() {
        decisionTable.getInputs().add(modelInstance.newInstance(Input.class));
        decisionTable.getInputs().add(modelInstance.newInstance(Input.class));
        decisionTable.getOutputs().add(modelInstance.newInstance(Output.class));
        final Rule rule = addRule(1, 2);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(2, validationResults.size());
        assertAll(
                () -> assertEquals(
                        "Rule has 1 input entries, but the decision table has 2 inputs",
                        validationResults.get(0).getMessage()),
                () -> assertEquals(
                        "Rule has 2 output entries, but the decision table has 1 outputs",
                        validationResults.get(1).getMessage()),
                () -> assertEquals(rule, validationResults.get(0).getElement()),
                () -> assertEquals(rule, validationResults.get(1).getElement()));
    }

    private Rule addRule(final int inputEntries, final int outputEntries) {
        final Rule rule = modelInstance.newInstance(Rule.class);
        for (int i = 0; i < inputEntries; i++) {
            rule.getInputEntries().add(modelInstance.newInstance(InputEntry.class));
        }
        for (int i = 0; i < outputEntries; i++) {
            rule.getOutputEntries().add(modelInstance.newInstance(OutputEntry.class));
        }
        decisionTable.getRules().add(rule);
        return rule;
    }
}
