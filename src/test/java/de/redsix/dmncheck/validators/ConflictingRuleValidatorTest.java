package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.validators.util.WithDecisionTable;
import org.camunda.bpm.model.dmn.HitPolicy;
import org.camunda.bpm.model.dmn.instance.InputEntry;
import org.camunda.bpm.model.dmn.instance.OutputEntry;
import org.camunda.bpm.model.dmn.instance.Rule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConflictingRuleValidatorTest extends WithDecisionTable {
    
    private final ConflictingRuleValidator testee = new ConflictingRuleValidator();

    @ParameterizedTest
    @EnumSource(value = HitPolicy.class, names = { "UNIQUE", "FIRST", "ANY"})
    void shouldDetectConflictingRulesForGivenHitPolicies(final HitPolicy hitPolicy) {
        decisionTable.setHitPolicy(hitPolicy);

        final InputEntry inputEntry = modelInstance.newInstance(InputEntry.class);
        inputEntry.setTextContent("foo");

        final InputEntry inputEntry2 = modelInstance.newInstance(InputEntry.class);
        inputEntry2.setTextContent("foo");

        final OutputEntry outputEntry = modelInstance.newInstance(OutputEntry.class);
        outputEntry.setTextContent("bar1");

        final OutputEntry outputEntry2 = modelInstance.newInstance(OutputEntry.class);
        outputEntry2.setTextContent("bar2");

        final Rule rule1 = modelInstance.newInstance(Rule.class);
        final Rule rule2 = modelInstance.newInstance(Rule.class);

        rule1.getInputEntries().add(inputEntry);
        rule2.getInputEntries().add(inputEntry2);

        rule1.getOutputEntries().add(outputEntry);
        rule2.getOutputEntries().add(outputEntry2);

        decisionTable.getRules().add(rule1);
        decisionTable.getRules().add(rule2);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.get(0);
        assertAll(
                () -> assertEquals("Rule is conflicting with rules [" + rule2.getId() + "]", validationResult.getMessage()),
                () -> assertEquals(rule1, validationResult.getElement()),
                () -> assertEquals(Severity.ERROR, validationResult.getSeverity())
        );
    }


    @ParameterizedTest
    @EnumSource(value = HitPolicy.class, names = { "COLLECT", "RULE_ORDER"})
    void shouldAllowConflictingRulesForGivenHitPolicies(final HitPolicy hitPolicy) {
        decisionTable.setHitPolicy(hitPolicy);

        final InputEntry inputEntry = modelInstance.newInstance(InputEntry.class);
        inputEntry.setTextContent("foo");

        final InputEntry inputEntry2 = modelInstance.newInstance(InputEntry.class);
        inputEntry2.setTextContent("foo");

        final OutputEntry outputEntry = modelInstance.newInstance(OutputEntry.class);
        outputEntry.setTextContent("bar1");

        final OutputEntry outputEntry2 = modelInstance.newInstance(OutputEntry.class);
        outputEntry2.setTextContent("bar2");

        final Rule rule1 = modelInstance.newInstance(Rule.class);
        final Rule rule2 = modelInstance.newInstance(Rule.class);

        rule1.getInputEntries().add(inputEntry);
        rule2.getInputEntries().add(inputEntry2);

        rule1.getOutputEntries().add(outputEntry);
        rule2.getOutputEntries().add(outputEntry2);

        decisionTable.getRules().add(rule1);
        decisionTable.getRules().add(rule2);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertTrue(validationResults.isEmpty());
    }

    @Test
    void shouldAllowDuplicateRules() {
        final InputEntry inputEntry = modelInstance.newInstance(InputEntry.class);
        inputEntry.setTextContent("foo");

        final InputEntry inputEntry2 = modelInstance.newInstance(InputEntry.class);
        inputEntry2.setTextContent("foo");

        final OutputEntry outputEntry = modelInstance.newInstance(OutputEntry.class);
        outputEntry.setTextContent("bar");

        final OutputEntry outputEntry2 = modelInstance.newInstance(OutputEntry.class);
        outputEntry2.setTextContent("bar");

        final Rule rule1 = modelInstance.newInstance(Rule.class);
        final Rule rule2 = modelInstance.newInstance(Rule.class);

        rule1.getInputEntries().add(inputEntry);
        rule2.getInputEntries().add(inputEntry2);

        rule1.getOutputEntries().add(outputEntry);
        rule2.getOutputEntries().add(outputEntry2);

        decisionTable.getRules().add(rule1);
        decisionTable.getRules().add(rule2);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertTrue(validationResults.isEmpty());
    }

}
