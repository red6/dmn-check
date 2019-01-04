package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.feel.ExpressionTypeParser;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.core.ValidationContext;
import org.camunda.bpm.model.dmn.instance.ItemDefinition;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ItemDefinitionAllowedValuesTypeValidator extends TypeValidator<ItemDefinition> {

    @Override
    public boolean isApplicable(ItemDefinition itemDefinition, ValidationContext validationContext) {
        final String expressionType = itemDefinition.getTypeRef().getTextContent();
        return itemDefinition.getAllowedValues() != null
                && ExpressionTypeParser.parse(expressionType, validationContext.getItemDefinitions()).match(parseError -> false, parseResult -> true);
    }

    @Override
    public List<ValidationResult> validate(ItemDefinition itemDefinition, ValidationContext validationContext) {

        final String expressionType = itemDefinition.getTypeRef().getTextContent();

        return ExpressionTypeParser.parse(expressionType, validationContext.getItemDefinitions())
                .match(validationResult -> Collections.singletonList(validationResult.element(itemDefinition).build()),
                        inputType -> typecheck(itemDefinition, Stream.of(itemDefinition.getAllowedValues()), Stream.of(inputType))
                                .collect(Collectors.toList()));
    }

    @Override
    protected Class<ItemDefinition> getClassUnderValidation() {
        return ItemDefinition.class;
    }

    @Override
    public String errorMessage() {
        return "Type of item definition does not match type of allowed values";
    }
}
