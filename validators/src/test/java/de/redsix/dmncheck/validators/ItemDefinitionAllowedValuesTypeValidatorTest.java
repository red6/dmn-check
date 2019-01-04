package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.util.WithDecisionTable;
import org.camunda.bpm.model.dmn.instance.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemDefinitionAllowedValuesTypeValidatorTest extends WithDecisionTable {

    private final ItemDefinitionAllowedValuesTypeValidator testee = new ItemDefinitionAllowedValuesTypeValidator();

    @Test
    void shouldAllowWelltypedAllowedValues() {
        final TypeRef typeRef = modelInstance.newInstance(TypeRef.class);
        typeRef.setTextContent("integer");

        final AllowedValues allowedValues = modelInstance.newInstance(AllowedValues.class);
        allowedValues.setTextContent("1, 2, 3");

        final ItemDefinition itemDefinition = modelInstance.newInstance(ItemDefinition.class);
        itemDefinition.setTypeRef(typeRef);
        itemDefinition.setAllowedValues(allowedValues);

        definitions.addChildElement(itemDefinition);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(0, validationResults.size());
    }

    @Test
    void shouldRejectIlltypedAllowedValues() {
        final TypeRef typeRef = modelInstance.newInstance(TypeRef.class);
        typeRef.setTextContent("string");

        final AllowedValues allowedValues = modelInstance.newInstance(AllowedValues.class);
        allowedValues.setTextContent("1, 2, 3");

        final ItemDefinition itemDefinition = modelInstance.newInstance(ItemDefinition.class);
        itemDefinition.setTypeRef(typeRef);
        itemDefinition.setAllowedValues(allowedValues);

        definitions.addChildElement(itemDefinition);

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