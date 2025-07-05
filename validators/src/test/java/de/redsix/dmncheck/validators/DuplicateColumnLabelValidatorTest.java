package de.redsix.dmncheck.validators;

import static org.junit.jupiter.api.Assertions.*;

import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.util.WithDecisionTable;
import java.util.List;
import org.camunda.bpm.model.dmn.instance.Input;
import org.camunda.bpm.model.dmn.instance.Output;
import org.junit.jupiter.api.Test;

class DuplicateColumnLabelValidatorTest extends WithDecisionTable {

    private final DuplicateColumnLabelValidator testee = new DuplicateColumnLabelValidator();

    @Test
    void outputsWithDistinctLabelsAreAllowed() {
        final Output output1 = modelInstance.newInstance(Output.class);
        output1.setLabel("Label1");
        decisionTable.getOutputs().add(output1);

        final Output output2 = modelInstance.newInstance(Output.class);
        output2.setLabel("Label2");
        decisionTable.getOutputs().add(output2);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(0, validationResults.size());
    }

    @Test
    void outputsWithIdenticalLabelsProduceWarnings() {
        final Output output1 = modelInstance.newInstance(Output.class);
        output1.setLabel("Label");
        decisionTable.getOutputs().add(output1);

        final Output output2 = modelInstance.newInstance(Output.class);
        output2.setLabel("Label");
        decisionTable.getOutputs().add(output2);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.getFirst();
        assertAll(
                () -> assertEquals("Column with label 'Label' is used more than once", validationResult.getMessage()),
                () -> assertEquals(decisionTable, validationResult.getElement()),
                () -> assertEquals(Severity.WARNING, validationResult.getSeverity()));
    }

    @Test
    void inputsWithDistinctLabelsAreAllowed() {
        final Input input1 = modelInstance.newInstance(Input.class);
        input1.setLabel("Label1");
        decisionTable.getInputs().add(input1);

        final Input input2 = modelInstance.newInstance(Input.class);
        input2.setLabel("Label2");
        decisionTable.getInputs().add(input2);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(0, validationResults.size());
    }

    @Test
    void inputsWithIdenticalLabelsProduceWarnings() {
        final Input input1 = modelInstance.newInstance(Input.class);
        input1.setLabel("Label");
        decisionTable.getInputs().add(input1);

        final Input input2 = modelInstance.newInstance(Input.class);
        input2.setLabel("Label");
        decisionTable.getInputs().add(input2);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.getFirst();
        assertAll(
                () -> assertEquals("Column with label 'Label' is used more than once", validationResult.getMessage()),
                () -> assertEquals(decisionTable, validationResult.getElement()),
                () -> assertEquals(Severity.WARNING, validationResult.getSeverity()));
    }
}
