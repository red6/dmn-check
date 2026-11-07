package de.redsix.dmncheck.validators;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.util.WithDecisionTable;
import java.util.List;
import org.camunda.bpm.model.dmn.HitPolicy;
import org.camunda.bpm.model.dmn.instance.Input;
import org.camunda.bpm.model.dmn.instance.InputEntry;
import org.camunda.bpm.model.dmn.instance.Output;
import org.camunda.bpm.model.dmn.instance.OutputEntry;
import org.camunda.bpm.model.dmn.instance.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class MergeableRulesValidatorTest extends WithDecisionTable {

    private final MergeableRulesValidator testee = new MergeableRulesValidator();

    @BeforeEach
    void prepareColumns() {
        final Input season = modelInstance.newInstance(Input.class);
        season.setLabel("Season");
        decisionTable.getInputs().add(season);
        decisionTable.getInputs().add(modelInstance.newInstance(Input.class));
        decisionTable.getOutputs().add(modelInstance.newInstance(Output.class));
    }

    @Test
    void shouldProposeToMergeRulesThatDifferInOneInput() {
        final Rule rule1 = addRule("\"Fall\"", "<= 8", "\"Stew\"");
        addRule("\"Spring\"", "> 8", "\"Steak\"");
        final Rule rule3 = addRule("\"Winter\"", "<= 8", "\"Stew\"");

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.getFirst();
        assertAll(
                () -> assertEquals(
                        "Rule can be merged with rule " + rule1.getId() + " as both only differ in input 'Season'. "
                                + "Use '\"Fall\", \"Winter\"' as input entry of the merged rule",
                        validationResult.getMessage()),
                () -> assertEquals(rule3, validationResult.getElement()),
                () -> assertEquals(Severity.WARNING, validationResult.getSeverity()));
    }

    @Test
    void shouldNameUnlabeledColumnsByNumber() {
        addRule("\"Fall\"", "<= 8", "\"Stew\"");
        addRule("\"Fall\"", "> 8", "\"Stew\"");

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(1, validationResults.size());
        assertEquals(
                "Rule can be merged with rule " + decisionTable.getRules().iterator().next().getId()
                        + " as both only differ in input 2. Use '<= 8, > 8' as input entry of the merged rule",
                validationResults.getFirst().getMessage());
    }

    @ParameterizedTest
    @CsvSource(
            delimiter = ';',
            value = {
                "'\"Fall\"'; '<= 8'; '\"Stew\"'; '\"Winter\"'; '> 8'; '\"Stew\"'",
                "'\"Fall\"'; '<= 8'; '\"Stew\"'; '\"Winter\"'; '<= 8'; '\"Steak\"'",
                "'\"Fall\"'; '<= 8'; '\"Stew\"'; '\"Fall\"'; '<= 8'; '\"Stew\"'",
                "'\"Fall\"'; '<= 8'; '\"Stew\"'; ''; '<= 8'; '\"Stew\"'",
                "'\"Fall\"'; '<= 8'; '\"Stew\"'; 'not(\"Winter\")'; '<= 8'; '\"Stew\"'",
                "'\"Fall\"'; '<= 8'; '\"Stew\"'; '[1..'; '<= 8'; '\"Stew\"'"
            })
    void shouldNotProposeToMergeOtherRules(
            final String input1,
            final String input2,
            final String output,
            final String otherInput1,
            final String otherInput2,
            final String otherOutput) {
        addRule(input1, input2, output);
        addRule(otherInput1, otherInput2, otherOutput);

        assertEquals(List.of(), testee.apply(modelInstance));
    }

    @ParameterizedTest
    @EnumSource(
            value = HitPolicy.class,
            names = {"UNIQUE", "ANY"},
            mode = EnumSource.Mode.EXCLUDE)
    void shouldOnlyProposeMergesForHitPoliciesUniqueAndAny(final HitPolicy hitPolicy) {
        decisionTable.setHitPolicy(hitPolicy);
        addRule("\"Fall\"", "<= 8", "\"Stew\"");
        addRule("\"Winter\"", "<= 8", "\"Stew\"");

        assertEquals(List.of(), testee.apply(modelInstance));
    }

    @Test
    void shouldProposeMergesForHitPolicyAny() {
        decisionTable.setHitPolicy(HitPolicy.ANY);
        addRule("\"Fall\"", "<= 8", "\"Stew\"");
        addRule("\"Winter\"", "<= 8", "\"Stew\"");

        assertEquals(1, testee.apply(modelInstance).size());
    }

    private Rule addRule(final String input1, final String input2, final String output) {
        final Rule rule = modelInstance.newInstance(Rule.class);
        for (final String input : List.of(input1, input2)) {
            final InputEntry inputEntry = modelInstance.newInstance(InputEntry.class);
            inputEntry.setTextContent(input);
            rule.getInputEntries().add(inputEntry);
        }
        final OutputEntry outputEntry = modelInstance.newInstance(OutputEntry.class);
        outputEntry.setTextContent(output);
        rule.getOutputEntries().add(outputEntry);
        decisionTable.getRules().add(rule);
        return rule;
    }
}
