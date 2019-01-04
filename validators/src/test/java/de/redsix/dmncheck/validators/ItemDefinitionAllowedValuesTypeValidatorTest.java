package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.util.WithItemDefinition;
import org.camunda.bpm.model.dmn.instance.AllowedValues;
import org.camunda.bpm.model.dmn.instance.TypeRef;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
    void shouldRejectIlltypedAllowedValues() {
        final TypeRef typeRef = modelInstance.newInstance(TypeRef.class);
        typeRef.setTextContent("string");

        final AllowedValues allowedValues = modelInstance.newInstance(AllowedValues.class);
        allowedValues.setTextContent("1, 2, 3");

        itemDefinition.setTypeRef(typeRef);
        itemDefinition.setAllowedValues(allowedValues);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.get(0);
        assertAll(
                () -> assertEquals("Type of item definition does not match type of allowed values",
                        validationResult.getMessage()),
                () -> assertEquals(itemDefinition, validationResult.getElement()),
                () -> assertEquals(Severity.ERROR, validationResult.getSeverity())
        );
    }

}