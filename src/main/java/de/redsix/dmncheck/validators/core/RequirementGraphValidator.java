package de.redsix.dmncheck.validators.core;

import de.redsix.dmncheck.result.ValidationResult;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.instance.AuthorityRequirement;
import org.camunda.bpm.model.dmn.instance.Decision;
import org.camunda.bpm.model.dmn.instance.DrgElement;
import org.camunda.bpm.model.dmn.instance.InformationRequirement;
import org.camunda.bpm.model.dmn.instance.InputData;
import org.camunda.bpm.model.dmn.instance.KnowledgeSource;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public abstract class RequirementGraphValidator implements Validator {

    public abstract List<ValidationResult> validate(DirectedAcyclicGraph<DrgElement, DefaultEdge> drg);

    @Override
    public List<ValidationResult> apply(DmnModelInstance dmnModelInstance) {
        final Collection<Decision> decisions = dmnModelInstance.getModelElementsByType(Decision.class);
        final Collection<KnowledgeSource> knowledgeSources = dmnModelInstance.getModelElementsByType(KnowledgeSource.class);
        final Collection<InputData> inputData = dmnModelInstance.getModelElementsByType(InputData.class);

        final DirectedAcyclicGraph<DrgElement, DefaultEdge> drg = new DirectedAcyclicGraph<>(DefaultEdge.class);

        Stream.of(decisions, knowledgeSources, inputData).flatMap(Collection::stream).forEach(drg::addVertex);

        for (Decision decision : decisions) {
            decision.getInformationRequirements()
                    .stream()
                    .flatMap(this::collectDrgElements)
                    .forEach(drgElement -> drg.addEdge(decision, drgElement));

            decision.getAuthorityRequirements()
                    .stream()
                    .flatMap(this::collectDrgElements)
                    .forEach(drgElement -> drg.addEdge(decision, drgElement));
        }

        for (KnowledgeSource knowledgeSource : knowledgeSources) {
            knowledgeSource.getAuthorityRequirement()
                    .stream()
                    .flatMap(this::collectDrgElements)
                    .forEach(drgElement -> drg.addEdge(knowledgeSource, drgElement));
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
