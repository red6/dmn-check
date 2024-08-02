package de.redsix.dmncheck.validators;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.util.WithDecisionTable;
import java.util.List;
import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.instance.*;
import org.junit.jupiter.api.Test;

class ConnectedRequirementGraphValidatorTest extends WithDecisionTable {

    private final ConnectedRequirementGraphValidator testee = new ConnectedRequirementGraphValidator();

    @Test
    void shouldDetectUnconnectedInputDataElement() {
        final InputData inputData = modelInstance.newInstance(InputData.class);
        definitions.addChildElement(inputData);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(2, validationResults.size());
        final ValidationResult validationResult1 = validationResults.get(0);
        assertAll(
                () -> assertEquals("Element is not connected to requirement graph", validationResult1.getMessage()),
                () -> assertEquals(decision, validationResult1.getElement()),
                () -> assertEquals(Severity.ERROR, validationResult1.getSeverity()));

        final ValidationResult validationResult2 = validationResults.get(1);
        assertAll(
                () -> assertEquals("Element is not connected to requirement graph", validationResult2.getMessage()),
                () -> assertEquals(inputData, validationResult2.getElement()),
                () -> assertEquals(Severity.ERROR, validationResult2.getSeverity()));
    }

    @Test
    void shouldAllowConnectedInputDataElement() {
        final InputData inputData = modelInstance.newInstance(InputData.class);
        definitions.addChildElement(inputData);

        // We access the just created InputData node to ensure it is loaded as InputDataImpl instead of
        // InputDataReferenceImpl. If we
        // do not do this and call InformationRequirement.setRequiredInput the id of our InputData node is associated
        // with an
        // InputDataReferenceImpl object. This issue seems to be related to CAM-8888 and CAM-8889. However, this issue
        // only occurs when
        // creating a DMN model programmatically using the parser of camunda-dmn-model everything is fine.
        modelInstance.getModelElementsByType(InputData.class);

        final InformationRequirement informationRequirement = modelInstance.newInstance(InformationRequirement.class);
        informationRequirement.setRequiredInput(inputData);

        decision.addChildElement(informationRequirement);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(0, validationResults.size());
    }

    @Test
    void shouldDetectUnconnectedKnowledgeSourceElement() {
        final KnowledgeSource knowledgeSource = modelInstance.newInstance(KnowledgeSource.class);
        definitions.addChildElement(knowledgeSource);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(2, validationResults.size());
        final ValidationResult validationResult1 = validationResults.get(0);
        assertAll(
                () -> assertEquals("Element is not connected to requirement graph", validationResult1.getMessage()),
                () -> assertEquals(decision, validationResult1.getElement()),
                () -> assertEquals(Severity.ERROR, validationResult1.getSeverity()));

        final ValidationResult validationResult2 = validationResults.get(1);
        assertAll(
                () -> assertEquals("Element is not connected to requirement graph", validationResult2.getMessage()),
                () -> assertEquals(knowledgeSource, validationResult2.getElement()),
                () -> assertEquals(Severity.ERROR, validationResult2.getSeverity()));
    }

    @Test
    void shouldAllowConnectedKnowledgeSourceElement() {
        final KnowledgeSource knowledgeSource = modelInstance.newInstance(KnowledgeSource.class);
        definitions.addChildElement(knowledgeSource);

        final AuthorityRequirement authorityRequirement = modelInstance.newInstance(AuthorityRequirement.class);
        authorityRequirement.setRequiredAuthority(knowledgeSource);

        decision.addChildElement(authorityRequirement);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(0, validationResults.size());
    }

    @Test
    void shouldAllowSingleDecision() {
        // Decision is created in the super class

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(0, validationResults.size());
    }

    @Test
    void shouldAllowTwoConnectedDecision() {
        final Output output = modelInstance.newInstance(Output.class);
        output.setName("someName");
        decisionTable.getOutputs().add(output);

        final Decision otherDecision = modelInstance.newInstance(Decision.class);

        final InformationRequirement informationRequirement = modelInstance.newInstance(InformationRequirement.class);
        informationRequirement.setRequiredDecision(decision);
        otherDecision.addChildElement(informationRequirement);

        final DecisionTable otherDecisionTable = modelInstance.newInstance(DecisionTable.class);
        final Input input = modelInstance.newInstance(Input.class);
        final InputExpression inputExpression = modelInstance.newInstance(InputExpression.class);
        inputExpression.setTextContent("someName");
        input.setInputExpression(inputExpression);
        otherDecisionTable.getInputs().add(input);
        otherDecision.addChildElement(otherDecisionTable);

        definitions.addChildElement(otherDecision);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(0, validationResults.size());
    }

    @Test
    void shouldAllowTwoConnectedDecisionWithFeelExpressionsAsInputs() {
        final Output output = modelInstance.newInstance(Output.class);
        output.setName("someName");
        decisionTable.getOutputs().add(output);

        final Decision otherDecision = modelInstance.newInstance(Decision.class);

        final InformationRequirement informationRequirement = modelInstance.newInstance(InformationRequirement.class);
        informationRequirement.setRequiredDecision(decision);
        otherDecision.addChildElement(informationRequirement);

        final DecisionTable otherDecisionTable = modelInstance.newInstance(DecisionTable.class);
        final Input input = modelInstance.newInstance(Input.class);
        final InputExpression inputExpression = modelInstance.newInstance(InputExpression.class);
        inputExpression.setTextContent("> someName");
        input.setInputExpression(inputExpression);
        otherDecisionTable.getInputs().add(input);
        otherDecision.addChildElement(otherDecisionTable);

        definitions.addChildElement(otherDecision);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(0, validationResults.size());
    }

    @Test
    void shouldDetectUnconnectedDecisionGraphs() {
        final KnowledgeSource knowledgeSource = modelInstance.newInstance(KnowledgeSource.class);
        definitions.addChildElement(knowledgeSource);

        final AuthorityRequirement authorityRequirement = modelInstance.newInstance(AuthorityRequirement.class);
        authorityRequirement.setRequiredAuthority(knowledgeSource);

        decision.addChildElement(authorityRequirement);

        final Decision otherDecision = modelInstance.newInstance(Decision.class);
        final DecisionTable otherDecisiontable = modelInstance.newInstance(DecisionTable.class);
        otherDecision.addChildElement(otherDecisiontable);

        final KnowledgeSource otherKnowledgeSource = modelInstance.newInstance(KnowledgeSource.class);
        definitions.addChildElement(otherKnowledgeSource);

        final AuthorityRequirement otherAuthorityRequirement = modelInstance.newInstance(AuthorityRequirement.class);
        otherAuthorityRequirement.setRequiredAuthority(otherKnowledgeSource);

        otherDecision.addChildElement(otherAuthorityRequirement);

        definitions.addChildElement(otherDecision);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.get(0);
        assertAll(
                () -> assertTrue(validationResult.getMessage().startsWith("Found unconnected requirement graphs:")),
                () -> assertEquals(definitions, validationResult.getElement()),
                () -> assertEquals(Severity.ERROR, validationResult.getSeverity()));
    }

    @Test
    void shouldDetectUnconnectedDecision() {
        final Decision otherDecision = modelInstance.newInstance(Decision.class);
        final DecisionTable otherDecisiontable = modelInstance.newInstance(DecisionTable.class);
        otherDecision.addChildElement(otherDecisiontable);

        definitions.addChildElement(otherDecision);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(2, validationResults.size());
        final ValidationResult validationResult1 = validationResults.get(0);
        assertAll(
                () -> assertEquals("Element is not connected to requirement graph", validationResult1.getMessage()),
                () -> assertEquals(decision, validationResult1.getElement()),
                () -> assertEquals(Severity.ERROR, validationResult1.getSeverity()));

        final ValidationResult validationResult2 = validationResults.get(1);
        assertAll(
                () -> assertEquals("Element is not connected to requirement graph", validationResult2.getMessage()),
                () -> assertEquals(otherDecision, validationResult2.getElement()),
                () -> assertEquals(Severity.ERROR, validationResult2.getSeverity()));
    }

    @Test
    void shouldDetectTwoConnectedDecisionWithInOutputsThatDoNotMatch() {
        final Output output = modelInstance.newInstance(Output.class);
        output.setName("someName");
        decisionTable.getOutputs().add(output);

        final Decision otherDecision = modelInstance.newInstance(Decision.class);

        final InformationRequirement informationRequirement = modelInstance.newInstance(InformationRequirement.class);
        informationRequirement.setRequiredDecision(decision);
        otherDecision.addChildElement(informationRequirement);

        final DecisionTable otherDecisionTable = modelInstance.newInstance(DecisionTable.class);
        final Input input = modelInstance.newInstance(Input.class);
        final InputExpression inputExpression = modelInstance.newInstance(InputExpression.class);
        inputExpression.setTextContent("someOtherName");
        input.setInputExpression(inputExpression);
        otherDecisionTable.getInputs().add(input);
        otherDecision.addChildElement(otherDecisionTable);

        definitions.addChildElement(otherDecision);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.get(0);
        assertAll(
                () -> assertEquals(
                        "Inputs and outputs do not match in connected decisions.", validationResult.getMessage()),
                () -> assertEquals(decision, validationResult.getElement()),
                () -> assertEquals(Severity.ERROR, validationResult.getSeverity()));
    }

    @Test
    void shouldDetectTwoConnectedDecisionWithInOutputsThatDoNotMatchWithFeelExpressionsAsInputs() {
        final Output output = modelInstance.newInstance(Output.class);
        output.setName("someName");
        decisionTable.getOutputs().add(output);

        final Decision otherDecision = modelInstance.newInstance(Decision.class);

        final InformationRequirement informationRequirement = modelInstance.newInstance(InformationRequirement.class);
        informationRequirement.setRequiredDecision(decision);
        otherDecision.addChildElement(informationRequirement);

        final DecisionTable otherDecisionTable = modelInstance.newInstance(DecisionTable.class);
        final Input input = modelInstance.newInstance(Input.class);
        final InputExpression inputExpression = modelInstance.newInstance(InputExpression.class);
        inputExpression.setTextContent("< someOtherName");
        input.setInputExpression(inputExpression);
        otherDecisionTable.getInputs().add(input);
        otherDecision.addChildElement(otherDecisionTable);

        definitions.addChildElement(otherDecision);

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.get(0);
        assertAll(
                () -> assertEquals(
                        "Inputs and outputs do not match in connected decisions.", validationResult.getMessage()),
                () -> assertEquals(decision, validationResult.getElement()),
                () -> assertEquals(Severity.ERROR, validationResult.getSeverity()));
    }

    @Test
    void shouldAcceptFileWithNoDecisions() {
        DmnModelInstance emptyModel = Dmn.createEmptyModel();

        Definitions emptyListOfDefinitions = emptyModel.newInstance(Definitions.class);
        emptyModel.setDefinitions(emptyListOfDefinitions);

        final List<ValidationResult> validationResults = testee.apply(emptyModel);

        assertEquals(0, validationResults.size());
    }
}
