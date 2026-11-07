package de.redsix.dmncheck.validators;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.util.WithDecisionTable;
import java.util.List;
import org.camunda.bpm.model.dmn.HitPolicy;
import org.camunda.bpm.model.dmn.instance.InputEntry;
import org.camunda.bpm.model.dmn.instance.Rule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class OverlappingRuleValidatorTest extends WithDecisionTable {

    private final OverlappingRuleValidator testee = new OverlappingRuleValidator();

    @Test
    void overlappingRulesAreReported() {
        final Rule rule = addRule("\"Winter\"", "<= 8");
        final Rule otherRule = addRule("\"Winter\"", "[5..10]");

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.getFirst();
        assertAll(
                () -> assertEquals(
                        "Rule overlaps with rule " + otherRule.getId()
                                + ", but hit policy UNIQUE requires disjoint rules",
                        validationResult.getMessage()),
                () -> assertEquals(rule, validationResult.getElement()),
                () -> assertEquals(Severity.ERROR, validationResult.getSeverity()));
    }

    @Test
    void uniqueIsTheDefaultHitPolicy() {
        addRule("", "<= 8");
        addRule("\"Winter\"", "[5..10]");

        assertEquals(1, testee.apply(modelInstance).size());
    }

    @Test
    void rulesThatAreDisjointInOneInputDoNotOverlap() {
        addRule("\"Winter\"", "<= 8");
        addRule("\"Winter\"", "> 8");
        addRule("\"Fall\"", "[5..10]");

        assertEquals(0, testee.apply(modelInstance).size());
    }

    @Test
    void rulesWithIdenticalInputsAreLeftToDuplicateAndConflictingRuleValidators() {
        addRule("\"Winter\"", "<= 8");
        addRule("\"Winter\"", "<= 8");

        assertEquals(0, testee.apply(modelInstance).size());
    }

    @Test
    void undecidableInputsAreNotReported() {
        addRule("\"Winter\"", "<= x");
        addRule("\"Winter\"", "[5..10]");

        assertEquals(0, testee.apply(modelInstance).size());
    }

    @Test
    void parsingErrorsAreNotReported() {
        addRule("\"Winter", "<= 8");
        addRule("\"Winter\"", "[5..10]");

        assertEquals(0, testee.apply(modelInstance).size());
    }

    @ParameterizedTest
    @EnumSource(
            value = HitPolicy.class,
            names = {"UNIQUE"},
            mode = EnumSource.Mode.EXCLUDE)
    void overlappingRulesAreAllowedForOtherHitPolicies(final HitPolicy hitPolicy) {
        decisionTable.setHitPolicy(hitPolicy);
        addRule("\"Winter\"", "<= 8");
        addRule("\"Winter\"", "[5..10]");

        assertEquals(0, testee.apply(modelInstance).size());
    }

    private Rule addRule(final String... inputs) {
        final Rule rule = modelInstance.newInstance(Rule.class);
        for (final String input : inputs) {
            final InputEntry inputEntry = modelInstance.newInstance(InputEntry.class);
            inputEntry.setTextContent(input);
            rule.getInputEntries().add(inputEntry);
        }
        decisionTable.getRules().add(rule);
        return rule;
    }
}
