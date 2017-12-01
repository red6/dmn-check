package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.result.ValidationResultType;
import de.redsix.dmncheck.validators.util.WithDecisionTable;
import org.camunda.bpm.model.dmn.instance.InputEntry;
import org.camunda.bpm.model.dmn.instance.OutputEntry;
import org.camunda.bpm.model.dmn.instance.Rule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConflictingRuleValidatorTest extends WithDecisionTable {

    @Test
    public void shouldAllowConflictingRules() {
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

        final List<ValidationResult> validationResults = ConflictingRuleValidator.instance.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.get(0);
        assertAll(
                () -> assertEquals("Rule is conflicting with rules [" + rule2.getId() + "]", validationResult.getMessage()),
                () -> assertEquals(rule1, validationResult.getElement()),
                () -> assertEquals(ValidationResultType.ERROR, validationResult.getValidationResultType())
        );
    }

    @Test
    public void shouldAllowDuplicateRules() {
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

        final List<ValidationResult> validationResults = ConflictingRuleValidator.instance.apply(modelInstance);

        assertTrue(validationResults.isEmpty());
    }

}
