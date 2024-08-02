package de.redsix.dmncheck.validators.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import java.util.Collections;
import java.util.List;
import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.instance.AllowedAnswers;
import org.camunda.bpm.model.dmn.instance.Decision;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.Definitions;
import org.camunda.bpm.model.dmn.instance.Description;
import org.camunda.bpm.model.dmn.instance.Input;
import org.camunda.bpm.model.dmn.instance.Question;
import org.camunda.bpm.model.dmn.instance.Rule;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GenericValidatorTest {

    @Test
    void anEmptyModelShouldProduceNoValidationResults() {
        // Arrange
        final GenericValidator<ModelElementInstance, ModelElementInstance> genericValidator =
                createTestee(ModelElementInstance.class);

        final DmnModelInstance dmnModelInstance = Dmn.createEmptyModel();

        when(genericValidator.isApplicable(any(), any())).thenReturn(true);

        final ModelElementInstance modelElementInstance = Mockito.mock(ModelElementInstance.class);
        final ValidationResult error = ValidationResult.init
                .message("")
                .severity(Severity.ERROR)
                .element(modelElementInstance)
                .build();
        when(genericValidator.validate(any(), any())).thenReturn(Collections.singletonList(error));

        // Act
        final List<ValidationResult> validationResults = genericValidator.apply(dmnModelInstance);

        // Assert
        assertTrue(validationResults.isEmpty());
    }

    @Test
    void allElementsApplicableEverythingIsValid() {
        // Arrange
        final GenericValidator<Definitions, Definitions> genericValidator = createTestee(Definitions.class);

        final DmnModelInstance dmnModelInstance = Dmn.createEmptyModel();

        final Definitions definitions = dmnModelInstance.newInstance(Definitions.class);
        dmnModelInstance.setDefinitions(definitions);

        when(genericValidator.isApplicable(any(Definitions.class), any(ValidationContext.class)))
                .thenReturn(true);
        when(genericValidator.validate(any(Definitions.class), any(ValidationContext.class)))
                .thenReturn(Collections.emptyList());

        // Act
        final List<ValidationResult> validationResults = genericValidator.apply(dmnModelInstance);

        // Assert
        assertTrue(validationResults.isEmpty());
    }

    @Test
    void noElementsApplicableEverythingIsInvalid() {
        // Arrange
        final GenericValidator<Definitions, Definitions> genericValidator = createTestee(Definitions.class);

        final DmnModelInstance dmnModelInstance = Dmn.createEmptyModel();

        final Definitions definitions = dmnModelInstance.newInstance(Definitions.class);
        dmnModelInstance.setDefinitions(definitions);

        when(genericValidator.isApplicable(any(Definitions.class), any(ValidationContext.class)))
                .thenReturn(false);

        final ValidationResult error = ValidationResult.init
                .message("")
                .severity(Severity.ERROR)
                .element(definitions)
                .build();
        when(genericValidator.validate(any(Definitions.class), any(ValidationContext.class)))
                .thenReturn(Collections.singletonList(error));

        // Act
        final List<ValidationResult> validationResults = genericValidator.apply(dmnModelInstance);

        // Assert
        assertTrue(validationResults.isEmpty());
    }

    @Test
    void allElementsApplicableEverythingIsInvalid() {
        // Arrange
        final GenericValidator<Definitions, Definitions> genericValidator = createTestee(Definitions.class);

        final DmnModelInstance dmnModelInstance = Dmn.createEmptyModel();

        final Definitions definitions = dmnModelInstance.newInstance(Definitions.class);
        dmnModelInstance.setDefinitions(definitions);

        when(genericValidator.isApplicable(any(Definitions.class), any(ValidationContext.class)))
                .thenReturn(true);

        final ValidationResult error = ValidationResult.init
                .message("")
                .severity(Severity.ERROR)
                .element(definitions)
                .build();
        when(genericValidator.validate(any(Definitions.class), any(ValidationContext.class)))
                .thenReturn(Collections.singletonList(error));

        // Act
        final List<ValidationResult> validationResults = genericValidator.apply(dmnModelInstance);

        // Assert
        assertEquals(1, validationResults.size());
        assertEquals(error, validationResults.get(0));
    }

    @Test
    void allElementsApplicableButEverythingIsInvalid() {
        // Arrange
        final GenericValidator<Definitions, Definitions> genericValidator = createTestee(Definitions.class);

        final DmnModelInstance dmnModelInstance = Dmn.createEmptyModel();

        final Definitions definitions = dmnModelInstance.newInstance(Definitions.class);
        dmnModelInstance.setDefinitions(definitions);

        when(genericValidator.isApplicable(any(Definitions.class), any(ValidationContext.class)))
                .thenReturn(true);

        final ValidationResult error = ValidationResult.init
                .message("")
                .severity(Severity.ERROR)
                .element(definitions)
                .build();
        when(genericValidator.validate(any(Definitions.class), any(ValidationContext.class)))
                .thenReturn(Collections.singletonList(error));

        // Act
        final List<ValidationResult> validationResults = genericValidator.apply(dmnModelInstance);

        // Assert
        assertEquals(1, validationResults.size());
        assertEquals(error, validationResults.get(0));
    }

    @Test
    void applicationCheckOnDecisionsAndValidationOnQuestions() {
        // Arrange
        final GenericValidator<Decision, Question> genericValidator = createTestee(Decision.class, Question.class);

        final DmnModelInstance dmnModelInstance = Dmn.createEmptyModel();

        final Definitions definitions = dmnModelInstance.newInstance(Definitions.class);
        dmnModelInstance.setDefinitions(definitions);

        final Decision decisionWithAllowedAnswers = dmnModelInstance.newInstance(Decision.class);
        definitions.addChildElement(decisionWithAllowedAnswers);

        final AllowedAnswers allowedAnswers = dmnModelInstance.newInstance(AllowedAnswers.class);
        decisionWithAllowedAnswers.setAllowedAnswers(allowedAnswers);

        final Decision decisionWithQuestion = dmnModelInstance.newInstance(Decision.class);
        definitions.addChildElement(decisionWithQuestion);

        final Question question = dmnModelInstance.newInstance(Question.class);
        decisionWithQuestion.setQuestion(question);

        when(genericValidator.isApplicable(any(Decision.class), any(ValidationContext.class)))
                .thenReturn(true);

        final ValidationResult error = ValidationResult.init
                .message("")
                .severity(Severity.ERROR)
                .element(question)
                .build();
        when(genericValidator.validate(any(Question.class), any(ValidationContext.class)))
                .thenReturn(Collections.singletonList(error));

        // Act
        final List<ValidationResult> validationResults = genericValidator.apply(dmnModelInstance);

        // Assert
        assertEquals(1, validationResults.size());
        assertEquals(error, validationResults.get(0));
    }

    @Test
    void validatesOnlyChildelementsOfApplicaleElements() {
        // Arrange
        final GenericValidator<Rule, Description> genericValidator = createTestee(Rule.class, Description.class);

        final DmnModelInstance dmnModelInstance = Dmn.createEmptyModel();

        final Definitions definitions = dmnModelInstance.newInstance(Definitions.class);
        dmnModelInstance.setDefinitions(definitions);

        final Decision decision = dmnModelInstance.newInstance(Decision.class);
        definitions.addChildElement(decision);

        final DecisionTable decisionTable = dmnModelInstance.newInstance(DecisionTable.class);

        final Rule rule = dmnModelInstance.newInstance(Rule.class);
        decisionTable.getRules().add(rule);

        final Description ruleDescription = dmnModelInstance.newInstance(Description.class);
        rule.setDescription(ruleDescription);

        final Input input = dmnModelInstance.newInstance(Input.class);
        decisionTable.getInputs().add(input);

        final Description inputDescription = dmnModelInstance.newInstance(Description.class);
        input.setDescription(inputDescription);

        when(genericValidator.isApplicable(any(Rule.class), any(ValidationContext.class)))
                .thenReturn(false);

        final ValidationResult error = ValidationResult.init
                .message("")
                .severity(Severity.ERROR)
                .element(ruleDescription)
                .build();
        when(genericValidator.validate(any(Description.class), any(ValidationContext.class)))
                .thenReturn(Collections.singletonList(error));

        // Act
        final List<ValidationResult> validationResults = genericValidator.apply(dmnModelInstance);

        // Assert
        assertTrue(validationResults.isEmpty());
    }

    private <T extends ModelElementInstance> GenericValidator<T, T> createTestee(Class<T> clazz) {
        return this.createTestee(clazz, clazz);
    }

    private <S extends ModelElementInstance, T extends ModelElementInstance> GenericValidator<S, T> createTestee(
            Class<S> applicableClass, Class<T> validationClass) {

        @SuppressWarnings("unchecked")
        final GenericValidator<S, T> genericValidator = (GenericValidator<S, T>) mock(GenericValidator.class);
        when(genericValidator.apply(any(DmnModelInstance.class))).thenCallRealMethod();
        when(genericValidator.getClassUsedToCheckApplicability()).thenReturn(applicableClass);
        when(genericValidator.getClassUnderValidation()).thenReturn(validationClass);

        return genericValidator;
    }
}
