package de.redsix.dmncheck.validators;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.util.WithDecisionTable;
import java.util.List;
import org.camunda.bpm.model.dmn.HitPolicy;
import org.camunda.bpm.model.dmn.instance.InputEntry;
import org.camunda.bpm.model.dmn.instance.OutputEntry;
import org.camunda.bpm.model.dmn.instance.Rule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DuplicateRuleValidatorTest extends WithDecisionTable {

    private final DuplicateRuleValidator testee = new DuplicateRuleValidator();

    @ParameterizedTest
    @CsvSource(
        {
            "COLLECT, WARNING",
            "UNIQUE, ERROR",
            "FIRST, ERROR",
            "PRIORITY, ERROR",
            "ANY, ERROR",
            "RULE_ORDER, ERROR",
            "OUTPUT_ORDER, ERROR",
        }
    )
    void shouldDetectDuplicateRule(
        final String hitpolicy,
        final String severity
    ) {
        decisionTable.setHitPolicy(HitPolicy.valueOf(hitpolicy));

        final InputEntry inputEntry = modelInstance.newInstance(
            InputEntry.class
        );
        inputEntry.setTextContent("foo");

        final InputEntry inputEntry2 = modelInstance.newInstance(
            InputEntry.class
        );
        inputEntry2.setTextContent("foo");

        final OutputEntry outputEntry = modelInstance.newInstance(
            OutputEntry.class
        );
        outputEntry.setTextContent("bar");

        final OutputEntry outputEntry2 = modelInstance.newInstance(
            OutputEntry.class
        );
        outputEntry2.setTextContent("bar");

        final Rule rule1 = modelInstance.newInstance(Rule.class);
        final Rule rule2 = modelInstance.newInstance(Rule.class);

        rule1.getInputEntries().add(inputEntry);
        rule2.getInputEntries().add(inputEntry2);

        rule1.getOutputEntries().add(outputEntry);
        rule2.getOutputEntries().add(outputEntry2);

        decisionTable.getRules().add(rule1);
        decisionTable.getRules().add(rule2);

        final List<ValidationResult> validationResults = testee.apply(
            modelInstance
        );

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.getFirst();
        assertAll(
            () ->
                assertEquals(
                    "Rule is defined more than once",
                    validationResult.getMessage()
                ),
            () -> assertEquals(rule2, validationResult.getElement()),
            () ->
                assertEquals(
                    Severity.valueOf(severity),
                    validationResult.getSeverity()
                )
        );
    }

    @Test
    void shouldAllowDistinguishableRules() {
        final InputEntry inputEntry = modelInstance.newInstance(
            InputEntry.class
        );
        inputEntry.setTextContent("foo1");

        final InputEntry inputEntry2 = modelInstance.newInstance(
            InputEntry.class
        );
        inputEntry2.setTextContent("foo2");

        final OutputEntry outputEntry = modelInstance.newInstance(
            OutputEntry.class
        );
        outputEntry.setTextContent("bar1");

        final OutputEntry outputEntry2 = modelInstance.newInstance(
            OutputEntry.class
        );
        outputEntry2.setTextContent("bar2");

        final Rule rule1 = modelInstance.newInstance(Rule.class);
        final Rule rule2 = modelInstance.newInstance(Rule.class);

        rule1.getInputEntries().add(inputEntry);
        rule2.getInputEntries().add(inputEntry2);

        rule1.getOutputEntries().add(outputEntry);
        rule2.getOutputEntries().add(outputEntry2);

        decisionTable.getRules().add(rule1);
        decisionTable.getRules().add(rule2);

        final List<ValidationResult> validationResults = testee.apply(
            modelInstance
        );

        assertTrue(validationResults.isEmpty());
    }

    @Test
    void shouldAllowConflictingRules() {
        final InputEntry inputEntry = modelInstance.newInstance(
            InputEntry.class
        );
        inputEntry.setTextContent("foo");

        final InputEntry inputEntry2 = modelInstance.newInstance(
            InputEntry.class
        );
        inputEntry2.setTextContent("foo");

        final OutputEntry outputEntry = modelInstance.newInstance(
            OutputEntry.class
        );
        outputEntry.setTextContent("bar1");

        final OutputEntry outputEntry2 = modelInstance.newInstance(
            OutputEntry.class
        );
        outputEntry2.setTextContent("bar2");

        final Rule rule1 = modelInstance.newInstance(Rule.class);
        final Rule rule2 = modelInstance.newInstance(Rule.class);

        rule1.getInputEntries().add(inputEntry);
        rule2.getInputEntries().add(inputEntry2);

        rule1.getOutputEntries().add(outputEntry);
        rule2.getOutputEntries().add(outputEntry2);

        decisionTable.getRules().add(rule1);
        decisionTable.getRules().add(rule2);

        final List<ValidationResult> validationResults = testee.apply(
            modelInstance
        );

        assertTrue(validationResults.isEmpty());
    }
}
