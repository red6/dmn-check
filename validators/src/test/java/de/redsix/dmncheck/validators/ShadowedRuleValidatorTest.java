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
import org.camunda.bpm.model.dmn.instance.Rule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ShadowedRuleValidatorTest extends WithDecisionTable {

    private final ShadowedRuleValidator testee = new ShadowedRuleValidator();

    @ParameterizedTest
    @EnumSource(value = HitPolicy.class, names = { "UNIQUE", "FIRST", "ANY" })
    void ruleIsShadowedByFirstRule(final HitPolicy hitPolicy) {
        decisionTable.setHitPolicy(hitPolicy);

        final InputEntry catchAllInputEntry = modelInstance.newInstance(
            InputEntry.class
        );
        catchAllInputEntry.setTextContent("");

        final InputEntry shadowedInputEntry = modelInstance.newInstance(
            InputEntry.class
        );
        shadowedInputEntry.setTextContent("");

        final Rule catchAllRule = modelInstance.newInstance(Rule.class);
        final Rule shadowedRule = modelInstance.newInstance(Rule.class);

        catchAllRule.getInputEntries().add(catchAllInputEntry);
        shadowedRule.getInputEntries().add(shadowedInputEntry);

        decisionTable.getRules().add(catchAllRule);
        decisionTable.getRules().add(shadowedRule);

        final List<ValidationResult> validationResults = testee.apply(
            modelInstance
        );

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.getFirst();
        assertAll(
            () ->
                assertEquals(
                    "Rule is shadowed by rule " + catchAllRule.getId(),
                    validationResult.getMessage()
                ),
            () -> assertEquals(shadowedRule, validationResult.getElement()),
            () -> assertEquals(Severity.ERROR, validationResult.getSeverity())
        );
    }

    @ParameterizedTest
    @EnumSource(value = HitPolicy.class, names = { "UNIQUE", "FIRST", "ANY" })
    void ruleIsNotShadowedByFirstRuleBecauseOfSecondInput(
        final HitPolicy hitPolicy
    ) {
        decisionTable.setHitPolicy(hitPolicy);

        final InputEntry catchAllInputEntry = modelInstance.newInstance(
            InputEntry.class
        );
        catchAllInputEntry.setTextContent("");

        final InputEntry shadowedInputEntry = modelInstance.newInstance(
            InputEntry.class
        );
        shadowedInputEntry.setTextContent("");

        final InputEntry textInputEntry = modelInstance.newInstance(
            InputEntry.class
        );
        textInputEntry.setTextContent("\"foo\"");

        final InputEntry differentTextInputEntry = modelInstance.newInstance(
            InputEntry.class
        );
        differentTextInputEntry.setTextContent("\"bar\"");

        final Rule rule = modelInstance.newInstance(Rule.class);
        final Rule otherRule = modelInstance.newInstance(Rule.class);

        rule.getInputEntries().add(catchAllInputEntry);
        rule.getInputEntries().add(textInputEntry);
        otherRule.getInputEntries().add(shadowedInputEntry);
        otherRule.getInputEntries().add(differentTextInputEntry);

        decisionTable.getRules().add(rule);
        decisionTable.getRules().add(otherRule);

        final List<ValidationResult> validationResults = testee.apply(
            modelInstance
        );

        assertEquals(0, validationResults.size());
    }

    @Test
    void detectsAndReportsParsingErrors() {
        final String invalidText = "\"invalid Text";

        decisionTable.setHitPolicy(HitPolicy.UNIQUE);

        final InputEntry catchAllInputEntry = modelInstance.newInstance(
            InputEntry.class
        );
        catchAllInputEntry.setTextContent(invalidText);

        final InputEntry shadowedInputEntry = modelInstance.newInstance(
            InputEntry.class
        );
        shadowedInputEntry.setTextContent(invalidText);

        final Rule catchAllRule = modelInstance.newInstance(Rule.class);
        final Rule shadowedRule = modelInstance.newInstance(Rule.class);

        catchAllRule.getInputEntries().add(catchAllInputEntry);
        shadowedRule.getInputEntries().add(shadowedInputEntry);

        decisionTable.getRules().add(catchAllRule);
        decisionTable.getRules().add(shadowedRule);

        final List<ValidationResult> validationResults = testee.apply(
            modelInstance
        );

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.getFirst();
        assertAll(
            () ->
                assertTrue(
                    validationResult.getMessage().contains("Could not parse")
                ),
            () -> assertEquals(shadowedRule, validationResult.getElement()),
            () -> assertEquals(Severity.ERROR, validationResult.getSeverity())
        );
    }

    @Test
    void warnsIfAnOtherExpressionLanguageThanFeelIsUsed() {
        final String text = "'foo'.repeat(6)";

        decisionTable.setHitPolicy(HitPolicy.UNIQUE);

        final InputEntry catchAllInputEntry = modelInstance.newInstance(
            InputEntry.class
        );
        catchAllInputEntry.setTextContent(text);
        catchAllInputEntry.setExpressionLanguage("javascript");

        final InputEntry shadowedInputEntry = modelInstance.newInstance(
            InputEntry.class
        );
        shadowedInputEntry.setTextContent(text);
        shadowedInputEntry.setExpressionLanguage("javascript");

        final Rule catchAllRule = modelInstance.newInstance(Rule.class);
        final Rule shadowedRule = modelInstance.newInstance(Rule.class);

        catchAllRule.getInputEntries().add(catchAllInputEntry);
        shadowedRule.getInputEntries().add(shadowedInputEntry);

        decisionTable.getRules().add(catchAllRule);
        decisionTable.getRules().add(shadowedRule);

        final List<ValidationResult> validationResults = testee.apply(
            modelInstance
        );

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.getFirst();
        assertAll(
            () ->
                assertTrue(
                    validationResult
                        .getMessage()
                        .contains(
                            "Expression language 'javascript' not supported"
                        )
                ),
            () -> assertEquals(shadowedRule, validationResult.getElement()),
            () -> assertEquals(Severity.WARNING, validationResult.getSeverity())
        );
    }
}
