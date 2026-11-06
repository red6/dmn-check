package de.redsix.dmncheck.validators;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.util.WithDecisionTable;
import java.util.List;
import org.camunda.bpm.model.dmn.instance.AllowedValues;
import org.camunda.bpm.model.dmn.instance.Input;
import org.camunda.bpm.model.dmn.instance.InputEntry;
import org.camunda.bpm.model.dmn.instance.InputExpression;
import org.camunda.bpm.model.dmn.instance.ItemDefinition;
import org.camunda.bpm.model.dmn.instance.Output;
import org.camunda.bpm.model.dmn.instance.OutputEntry;
import org.camunda.bpm.model.dmn.instance.Rule;
import org.camunda.bpm.model.dmn.instance.TypeRef;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AllowedValuesValidatorTest extends WithDecisionTable {

    private final AllowedValuesValidator testee = new AllowedValuesValidator();

    @ParameterizedTest
    @CsvSource(
            delimiter = ';',
            value = {
                "\"Fall\",\"Winter\";\"Fall\"",
                "\"Fall\",\"Winter\";\"Fall\", \"Winter\"",
                "\"Fall\",\"Winter\";''",
                "\"Fall\",\"Winter\";not(\"Summer\")",
                "\"Fall\",\"Winter\";season",
                "[1..10];5",
                "[1..10];> 20",
                "1, 2, 3;3",
                "< 10;9",
                "< 10;10",
                "not(\"Summer\");\"Fall\"",
                "[1.0..10.0];5",
                "[\"A\"..\"M\"];\"B\"",
                "\"Fall\",\"Winter\";\"Fall"
            })
    void allowedOrUndecidableInputEntriesAreNotReported(final String allowedValues, final String inputEntry) {
        addInputColumn(addItemDefinition("season", "string", allowedValues));
        addRule(inputEntry);

        assertEquals(0, testee.apply(modelInstance).size());
    }

    @ParameterizedTest
    @CsvSource(
            delimiter = ';',
            value = {
                "\"Fall\",\"Winter\";\"Summer\"",
                "\"Fall\",\"Winter\";\"Fall\", \"Summer\"",
                "[1..10];11",
                "1, 2, 3;4",
                "not(\"Summer\");\"Summer\"",
                "true;false"
            })
    void disallowedInputEntriesAreReported(final String allowedValues, final String inputEntry) {
        addInputColumn(addItemDefinition("season", "string", allowedValues));
        final Rule rule = addRule(inputEntry);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.getFirst();
        assertAll(
                () -> assertEquals(
                        "Value " + inputEntry + " is not allowed by item definition season",
                        validationResult.getMessage()),
                () -> assertEquals(rule, validationResult.getElement()),
                () -> assertEquals(Severity.ERROR, validationResult.getSeverity()));
    }

    @Test
    void disallowedOutputEntriesAreReported() {
        final Output output = modelInstance.newInstance(Output.class);
        output.setTypeRef(addItemDefinition("dish", "string", "\"Steak\", \"Stew\"").getName());
        decisionTable.getOutputs().add(output);

        final Rule rule = addRule();
        final OutputEntry outputEntry = modelInstance.newInstance(OutputEntry.class);
        outputEntry.setTextContent("\"Salad\"");
        rule.getOutputEntries().add(outputEntry);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(1, validationResults.size());
        assertEquals(
                "Value \"Salad\" is not allowed by item definition dish",
                validationResults.getFirst().getMessage());
    }

    @Test
    void columnsWithoutItemDefinitionAreNotChecked() {
        addItemDefinition("season", "string", "\"Fall\"");
        final Input input = modelInstance.newInstance(Input.class);
        final InputExpression inputExpression = modelInstance.newInstance(InputExpression.class);
        inputExpression.setTypeRef("string");
        input.setInputExpression(inputExpression);
        decisionTable.getInputs().add(input);
        addRule("\"Summer\"");

        assertEquals(0, testee.apply(modelInstance).size());
    }

    private ItemDefinition addItemDefinition(final String name, final String type, final String allowedValuesText) {
        final ItemDefinition itemDefinition = modelInstance.newInstance(ItemDefinition.class);
        itemDefinition.setName(name);

        final TypeRef typeRef = modelInstance.newInstance(TypeRef.class);
        typeRef.setTextContent(type);
        itemDefinition.setTypeRef(typeRef);

        final AllowedValues allowedValues = modelInstance.newInstance(AllowedValues.class);
        allowedValues.setTextContent(allowedValuesText);
        itemDefinition.setAllowedValues(allowedValues);

        definitions.addChildElement(itemDefinition);
        return itemDefinition;
    }

    private void addInputColumn(final ItemDefinition itemDefinition) {
        final Input input = modelInstance.newInstance(Input.class);
        final InputExpression inputExpression = modelInstance.newInstance(InputExpression.class);
        inputExpression.setTypeRef(itemDefinition.getName());
        input.setInputExpression(inputExpression);
        decisionTable.getInputs().add(input);
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
