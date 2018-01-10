package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.result.ValidationResultType;
import de.redsix.dmncheck.validators.util.WithDecisionTable;
import org.camunda.bpm.model.dmn.HitPolicy;
import org.camunda.bpm.model.dmn.instance.InputEntry;
import org.camunda.bpm.model.dmn.instance.Rule;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ShadowedRuleValidatorTest extends WithDecisionTable {

    @ParameterizedTest
    @EnumSource(value = HitPolicy.class, names = { "UNIQUE", "FIRST", "ANY"})
    public void RuleIsShadowedByFirstRule(final HitPolicy hitPolicy) {
        decisionTable.setHitPolicy(hitPolicy);

        final InputEntry catchAllInputEntry = modelInstance.newInstance(InputEntry.class);
        catchAllInputEntry.setTextContent("");

        final InputEntry shadowedInputEntry = modelInstance.newInstance(InputEntry.class);
        shadowedInputEntry.setTextContent("");

        final Rule catchAllRule = modelInstance.newInstance(Rule.class);
        final Rule shadowedRule = modelInstance.newInstance(Rule.class);

        catchAllRule.getInputEntries().add(catchAllInputEntry);
        shadowedRule.getInputEntries().add(shadowedInputEntry);

        decisionTable.getRules().add(catchAllRule);
        decisionTable.getRules().add(shadowedRule);

        final List<ValidationResult> validationResults = ShadowedRuleValidator.instance.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.get(0);
        assertAll(
                () -> assertEquals("Rule is shadowed by rule " + catchAllRule.getId(), validationResult.getMessage()),
                () -> assertEquals(shadowedRule, validationResult.getElement()),
                () -> assertEquals(ValidationResultType.ERROR, validationResult.getValidationResultType())
        );
    }

}
