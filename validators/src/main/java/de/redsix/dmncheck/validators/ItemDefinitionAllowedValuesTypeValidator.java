package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.feel.ExpressionTypeParser;
import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.core.ValidationContext;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.camunda.bpm.model.dmn.instance.ItemDefinition;

public class ItemDefinitionAllowedValuesTypeValidator
    extends TypeValidator<ItemDefinition> {

    @Override
    public boolean isApplicable(
        ItemDefinition itemDefinition,
        ValidationContext validationContext
    ) {
        return (
            itemDefinition.getAllowedValues() != null ||
            itemDefinition
                .getItemComponents()
                .stream()
                .anyMatch(
                    itemComponent -> itemComponent.getAllowedValues() != null
                )
        );
    }

    @Override
    public List<ValidationResult> validate(
        ItemDefinition itemDefinition,
        ValidationContext validationContext
    ) {
        final Collection<ItemDefinition> itemDefinitionsAndComponents =
            itemDefinition
                .getItemComponents()
                .stream()
                .filter(
                    itemComponent -> itemComponent.getAllowedValues() != null
                )
                .collect(Collectors.toList());

        if (itemDefinition.getAllowedValues() != null) {
            itemDefinitionsAndComponents.add(itemDefinition);
        }

        return itemDefinitionsAndComponents
            .stream()
            .flatMap(itemDefinitionOrComponent -> {
                if (itemDefinitionOrComponent.getTypeRef() == null) {
                    return Stream.of(
                        ValidationResult.init
                            .message(
                                "ItemDefintion uses AllowedValues without a type declaration"
                            )
                            .severity(Severity.WARNING)
                            .element(itemDefinitionOrComponent)
                            .build()
                    );
                } else {
                    final String expressionType = itemDefinitionOrComponent
                        .getTypeRef()
                        .getTextContent();
                    return ExpressionTypeParser.parse(
                        expressionType,
                        validationContext.getItemDefinitions()
                    ).match(
                        validationResult ->
                            Stream.of(
                                validationResult
                                    .element(itemDefinitionOrComponent)
                                    .build()
                            ),
                        inputType ->
                            typecheck(
                                itemDefinitionOrComponent,
                                Stream.of(
                                    itemDefinitionOrComponent.getAllowedValues()
                                ).map(toplevelExpressionLanguage::toExpression),
                                Stream.of(inputType)
                            )
                    );
                }
            })
            .collect(Collectors.toList());
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
