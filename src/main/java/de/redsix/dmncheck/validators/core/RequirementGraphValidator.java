package de.redsix.dmncheck.validators.core;

import de.redsix.dmncheck.drg.RequirementGraph;
import de.redsix.dmncheck.result.ValidationResult;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.instance.AuthorityRequirement;
import org.camunda.bpm.model.dmn.instance.Decision;
import org.camunda.bpm.model.dmn.instance.Definitions;
import org.camunda.bpm.model.dmn.instance.DrgElement;
import org.camunda.bpm.model.dmn.instance.InformationRequirement;
import org.camunda.bpm.model.dmn.instance.InputData;
import org.camunda.bpm.model.dmn.instance.KnowledgeSource;
import org.jgrapht.graph.DefaultEdge;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public abstract class RequirementGraphValidator implements Validator {

    public abstract List<ValidationResult> validate(RequirementGraph drg);

    @Override
    public List<ValidationResult> apply(DmnModelInstance dmnModelInstance) {
        final Collection<Decision> decisions = dmnModelInstance.getModelElementsByType(Decision.class);
        final Collection<KnowledgeSource> knowledgeSources = dmnModelInstance.getModelElementsByType(KnowledgeSource.class);
        final Collection<InputData> inputData = dmnModelInstance.getModelElementsByType(InputData.class);

        final RequirementGraph drg = new RequirementGraph(DefaultEdge.class, dmnModelInstance.getDefinitions());

        Stream.of(decisions, knowledgeSources, inputData).flatMap(Collection::stream).forEach(drg::addVertex);

        try {
            for (Decision decision : decisions) {
                decision.getInformationRequirements().stream()
                        .flatMap(this::collectDrgElements)
                        .forEach(drgElement -> drg.addEdge(drgElement, decision));

                decision.getAuthorityRequirements().stream()
                        .flatMap(this::collectDrgElements)
                        .forEach(drgElement -> drg.addEdge(drgElement, decision));
            }

            for (KnowledgeSource knowledgeSource : knowledgeSources) {
                knowledgeSource.getAuthorityRequirement().stream()
                        .flatMap(this::collectDrgElements)
                        .forEach(drgElement -> drg.addEdge(drgElement, knowledgeSource));
            }
        } catch (IllegalArgumentException exception) {
            return Collections.singletonList(ValidationResult.init
                    .message("Error while construction requirement graph: " + exception.getMessage())
                    .element(dmnModelInstance.getDefinitions())
                    .build());
        }

        return validate(drg);
    }

    private Stream<DrgElement> collectDrgElements(InformationRequirement informationRequirement) {
        return Stream.of(informationRequirement.getRequiredDecision(), informationRequirement.getRequiredInput())
                .filter(Objects::nonNull);
    }

    private Stream<DrgElement> collectDrgElements(AuthorityRequirement authorityRequirement) {
        return Stream.of(authorityRequirement.getRequiredDecision(),
                authorityRequirement.getRequiredInput(),
                authorityRequirement.getRequiredAuthority())
                .filter(Objects::nonNull);
    }
}
