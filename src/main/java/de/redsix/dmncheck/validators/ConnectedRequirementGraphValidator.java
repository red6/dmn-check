package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.core.SimpleValidator;
import org.camunda.bpm.model.dmn.instance.AuthorityRequirement;
import org.camunda.bpm.model.dmn.instance.Decision;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.Definitions;
import org.camunda.bpm.model.dmn.instance.InformationRequirement;
import org.camunda.bpm.model.dmn.instance.Input;
import org.camunda.bpm.model.dmn.instance.InputData;
import org.camunda.bpm.model.dmn.instance.InputExpression;
import org.camunda.bpm.model.dmn.instance.KnowledgeSource;
import org.camunda.bpm.model.dmn.instance.OutputClause;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConnectedRequirementGraphValidator extends SimpleValidator<Definitions> {

    @Override
    public boolean isApplicable(Definitions definitions) {
        return true;
    }

    @Override
    public List<ValidationResult> validate(Definitions definitions) {
        final List<ValidationResult> validationResults = new ArrayList<>();
        final Collection<Decision> decisions = definitions.getModelInstance().getModelElementsByType(Decision.class);

        unreferencedElements(InputData.class,
                decision -> decision.getInformationRequirements().stream().map(InformationRequirement::getRequiredInput),
                definitions, decisions)
                .forEach(inputData ->
                        validationResults.add(ValidationResult.init
                                .message("Input " + inputData.getName() + " is not connect to any decision.")
                                .element(inputData)
                                .build())
                );

        unreferencedElements(KnowledgeSource.class,
                decision -> decision.getAuthorityRequirements().stream().map(AuthorityRequirement::getRequiredAuthority),
                definitions, decisions)
                .forEach( knowledgeSource ->
                        validationResults.add(ValidationResult.init
                                .message("Knowledge Source " + knowledgeSource.getName() + " is not connect to any decision.")
                                .element(knowledgeSource)
                                .build())
                );

        if (unreferencedElements(Decision.class,
                decision -> decision.getInformationRequirements().stream().map(InformationRequirement::getRequiredDecision),
                definitions, decisions).size() > 1) {
            validationResults.add(ValidationResult.init
                    .message("The following decisions are not connected to an other decision: " + decisions)
                    .element(definitions)
                    .build());
        }

        validationResults.addAll(validateConnectedDecisions(decisions));

        return validationResults;
    }

    private <T extends ModelElementInstance> Collection<T> unreferencedElements(Class<T> clazz, Function<Decision, Stream<T>> extract, Definitions definitions, Collection<Decision> decisions) {
        final Collection<T> declaredElements = definitions.getModelInstance().getModelElementsByType(clazz);

        final Set<T> requiredElementsInDecisions = decisions.stream()
                .flatMap(extract::apply)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        declaredElements.removeAll(requiredElementsInDecisions);

        return declaredElements;
    }

    private List<ValidationResult> validateConnectedDecisions(Collection<Decision> decisions) {
        final List<ValidationResult> validationResults = new ArrayList<>();

        decisions.forEach(decision ->
            applyOnDecsionTable(decision, decisionTable -> {
                final Set<String> inputIds = decisionTable.getInputs().stream()
                        .map(Input::getInputExpression)
                        .map(InputExpression::getTextContent)
                        .collect(Collectors.toSet());

                decision.getInformationRequirements().stream()
                        .map(InformationRequirement::getRequiredDecision)
                        .filter(Objects::nonNull)
                        .forEach(requiredDecision ->
                            applyOnDecsionTable(requiredDecision, requiredDecisionTable -> {
                                        final Set<String> outputIds = requiredDecisionTable.getOutputs().stream()
                                                .map(OutputClause::getName)
                                                .collect(Collectors.toSet());

                                        inputIds.retainAll(outputIds);

                                        if (inputIds.isEmpty()) {
                                            validationResults.add(ValidationResult.init
                                                    .message("Inputs and outputs do not match in connected decisions.")
                                                    .element(decision)
                                                    .build());
                                        }
                                    }
                            ).ifPresent(validationResults::add)
                        );
            }).ifPresent(validationResults::add)
        );

        return validationResults;
    }

    private Optional<ValidationResult> applyOnDecsionTable(Decision decision, Consumer<DecisionTable> consumer) {
        final Collection<DecisionTable> decisionTables = decision.getChildElementsByType(DecisionTable.class);

        if (decisionTables.size() == 1) {
            consumer.accept(decisionTables.iterator().next());
            return Optional.empty();
        } else {
            return Optional.of(ValidationResult.init
                    .message("There is either no or more than one decision table.")
                    .element(decision)
                    .build());
        }
    }

    @Override
    public Class<Definitions> getClassUnderValidation() {
        return Definitions.class;
    }
}
