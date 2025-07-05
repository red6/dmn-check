package de.redsix.dmncheck.validators;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.util.WithDecisionTable;
import java.util.List;
import org.camunda.bpm.model.dmn.instance.InputData;
import org.junit.jupiter.api.Test;

class InputDataIdAndNameValidatorTest extends WithDecisionTable {

    private final InputDataIdAndNameValidator testee = new InputDataIdAndNameValidator();

    @Test
    void shouldErrorIfInputDataHasNoId() {
        final InputData inputData = modelInstance.newInstance(InputData.class);
        definitions.addChildElement(inputData);

        // We access the just created InputData node to ensure it is loaded as InputDataImpl instead of
        // InputDataReferenceImpl. This issue
        // seems to be related to CAM-8888 and CAM-8889. However, this issue only occurs when creating a DMN model
        // programmatically using
        // the parser of camunda-dmn-model everything is fine.
        modelInstance.getModelElementsByType(InputData.class);

        inputData.setId(null);
        inputData.setName("Test");

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.getFirst();
        assertAll(
                () -> assertEquals("A input has no id.", validationResult.getMessage()),
                () -> assertEquals(inputData, validationResult.getElement()),
                () -> assertEquals(Severity.ERROR, validationResult.getSeverity()));
    }

    @Test
    void shouldWarnIfInputDataHasNoName() {
        final InputData inputData = modelInstance.newInstance(InputData.class);
        definitions.addChildElement(inputData);

        // We access the just created InputData node to ensure it is loaded as InputDataImpl instead of
        // InputDataReferenceImpl. This issue
        // seems to be related to CAM-8888 and CAM-8889. However, this issue only occurs when creating a DMN model
        // programmatically using
        // the parser of camunda-dmn-model everything is fine.
        modelInstance.getModelElementsByType(InputData.class);

        inputData.setId("1");
        inputData.setName(null);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.getFirst();
        assertAll(
                () -> assertEquals("A input has no name.", validationResult.getMessage()),
                () -> assertEquals(inputData, validationResult.getElement()),
                () -> assertEquals(Severity.WARNING, validationResult.getSeverity()));
    }

    @Test
    void shouldAllowInputDataWithIdAndname() {
        final InputData inputData = modelInstance.newInstance(InputData.class);
        definitions.addChildElement(inputData);

        // We access the just created InputData node to ensure it is loaded as InputDataImpl instead of
        // InputDataReferenceImpl. This issue
        // seems to be related to CAM-8888 and CAM-8889. However, this issue only occurs when creating a DMN model
        // programmatically using
        // the parser of camunda-dmn-model everything is fine.
        modelInstance.getModelElementsByType(InputData.class);

        inputData.setId("1");
        inputData.setName("Test");

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertTrue(validationResults.isEmpty());
    }
}
