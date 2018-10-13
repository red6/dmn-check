package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.core.SimpleValidator;
import org.camunda.bpm.model.dmn.instance.*;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

import java.util.*;
import java.util.List;
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

    @Override
    public Class<Definitions> getClassUnderValidation() {
        return Definitions.class;
    }
}
