package de.redsix.dmncheck.validators;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.util.WithItemDefinition;
import java.util.List;
import org.camunda.bpm.model.dmn.instance.AllowedValues;
import org.camunda.bpm.model.dmn.instance.ItemComponent;
import org.camunda.bpm.model.dmn.instance.TypeRef;
import org.junit.jupiter.api.Test;

class ItemDefinitionAllowedValuesTypeValidatorTest extends WithItemDefinition {

    private final ItemDefinitionAllowedValuesTypeValidator testee = new ItemDefinitionAllowedValuesTypeValidator();

    @Test
    void shouldAllowWelltypedAllowedValues() {
        final TypeRef typeRef = modelInstance.newInstance(TypeRef.class);
        typeRef.setTextContent("integer");

        final AllowedValues allowedValues = modelInstance.newInstance(AllowedValues.class);
        allowedValues.setTextContent("1, 2, 3");

        itemDefinition.setTypeRef(typeRef);
        itemDefinition.setAllowedValues(allowedValues);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(0, validationResults.size());
    }

    @Test
    void shouldAllowWelltypedItemComponents() {
        final TypeRef typeRefInteger = modelInstance.newInstance(TypeRef.class);
        typeRefInteger.setTextContent("integer");

        final AllowedValues allowedValuesInteger = modelInstance.newInstance(AllowedValues.class);
        allowedValuesInteger.setTextContent("1, 2, 3");

        final ItemComponent itemComponentInteger = modelInstance.newInstance(ItemComponent.class);
        itemComponentInteger.setTypeRef(typeRefInteger);
        itemComponentInteger.setAllowedValues(allowedValuesInteger);

        itemDefinition.addChildElement(itemComponentInteger);

        final TypeRef typeRefBoolean = modelInstance.newInstance(TypeRef.class);
        typeRefBoolean.setTextContent("boolean");

        final AllowedValues allowedValuesBoolean = modelInstance.newInstance(AllowedValues.class);
        allowedValuesBoolean.setTextContent("true");

        final ItemComponent itemComponentBoolean = modelInstance.newInstance(ItemComponent.class);
        itemComponentBoolean.setTypeRef(typeRefBoolean);
        itemComponentBoolean.setAllowedValues(allowedValuesBoolean);

        itemDefinition.addChildElement(itemComponentBoolean);

        final ItemComponent itemComponentWithoutAllowedValues = modelInstance.newInstance(ItemComponent.class);
        itemDefinition.addChildElement(itemComponentWithoutAllowedValues);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(0, validationResults.size());
    }

    @Test
    void shouldWarnAboutMissingTypeDeclaration() {
        final AllowedValues allowedValues = modelInstance.newInstance(AllowedValues.class);
        allowedValues.setTextContent("1, 2, 3");

        itemDefinition.setAllowedValues(allowedValues);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.getFirst();
        assertAll(
                () -> assertEquals(
                        "ItemDefintion uses AllowedValues without a type declaration", validationResult.getMessage()),
                () -> assertEquals(itemDefinition, validationResult.getElement()),
                () -> assertEquals(Severity.WARNING, validationResult.getSeverity()));
    }

    @Test
    void shouldRejectIlltypedAllowedValues() {
        final TypeRef typeRef = modelInstance.newInstance(TypeRef.class);
        typeRef.setTextContent("string");

        final AllowedValues allowedValues = modelInstance.newInstance(AllowedValues.class);
        allowedValues.setTextContent("1, 2, 3");

        itemDefinition.setTypeRef(typeRef);
        itemDefinition.setAllowedValues(allowedValues);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.getFirst();
        assertAll(
                () -> assertEquals(
                        "Type of item definition does not match type of allowed values", validationResult.getMessage()),
                () -> assertEquals(itemDefinition, validationResult.getElement()),
                () -> assertEquals(Severity.ERROR, validationResult.getSeverity()));
    }

    @Test
    void warnsIfAnOtherExpressionLanguageThanFeelIsUsed() {
        final TypeRef typeRef = modelInstance.newInstance(TypeRef.class);
        typeRef.setTextContent("string");

        final AllowedValues allowedValues = modelInstance.newInstance(AllowedValues.class);
        allowedValues.setTextContent("'foo'.repeat(6)");
        allowedValues.setExpressionLanguage("javascript");

        itemDefinition.setTypeRef(typeRef);
        itemDefinition.setAllowedValues(allowedValues);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.getFirst();
        assertAll(
                () -> assertEquals("Expression language 'javascript' not supported", validationResult.getMessage()),
                () -> assertEquals(itemDefinition, validationResult.getElement()),
                () -> assertEquals(Severity.WARNING, validationResult.getSeverity()));
    }
}
