package de.redsix.dmncheck.drg;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.instance.AuthorityRequirement;
import org.camunda.bpm.model.dmn.instance.Decision;
import org.camunda.bpm.model.dmn.instance.Definitions;
import org.camunda.bpm.model.dmn.instance.DrgElement;
import org.camunda.bpm.model.dmn.instance.InformationRequirement;
import org.camunda.bpm.model.dmn.instance.InputData;
import org.camunda.bpm.model.dmn.instance.KnowledgeSource;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;

public class RequirementGraph extends DirectedAcyclicGraph<DrgElement, DefaultEdge> {

    private final transient Definitions definitions;

    public RequirementGraph(Class<? extends DefaultEdge> edgeClass, Definitions definitions) {
        super(edgeClass);
        this.definitions = definitions;
    }

    public Definitions getDefinitions() {
        return definitions;
    }

    public static RequirementGraph from(final DmnModelInstance dmnModelInstance) throws IllegalArgumentException {
        final Collection<Decision> decisions = dmnModelInstance.getModelElementsByType(Decision.class);
        final Collection<KnowledgeSource> knowledgeSources =
                dmnModelInstance.getModelElementsByType(KnowledgeSource.class);
        final Collection<InputData> inputData = dmnModelInstance.getModelElementsByType(InputData.class);

        final RequirementGraph drg = new RequirementGraph(DefaultEdge.class, dmnModelInstance.getDefinitions());

        // checkerframework cannot figure out the types in this case without an explicit declaration
        // found   : @UnknownRegex Stream<?[ extends @UnknownRegex DrgElement super @UnknownRegex Void]>
        // required: @UnknownRegex Stream<?[ extends capture#385[ extends @UnknownRegex DrgElement super @RegexBottom
        // Void] super @RegexBottom Void]>
        Stream.<Collection<? extends DrgElement>>of(decisions, knowledgeSources, inputData)
                .flatMap(Collection::stream)
                .forEach(drg::addVertex);

        for (Decision decision : decisions) {
            decision.getInformationRequirements().stream()
                    .flatMap(RequirementGraph::collectDrgElements)
                    .forEach(drgElement -> drg.addEdge(drgElement, decision));

            decision.getAuthorityRequirements().stream()
                    .flatMap(RequirementGraph::collectDrgElements)
                    .forEach(drgElement -> drg.addEdge(drgElement, decision));
        }

        for (KnowledgeSource knowledgeSource : knowledgeSources) {
            knowledgeSource.getAuthorityRequirement().stream()
                    .flatMap(RequirementGraph::collectDrgElements)
                    .forEach(drgElement -> drg.addEdge(drgElement, knowledgeSource));
        }

        return drg;
    }

    private static Stream<DrgElement> collectDrgElements(InformationRequirement informationRequirement) {
        return Stream.of(informationRequirement.getRequiredDecision(), informationRequirement.getRequiredInput())
                .filter(Objects::nonNull);
    }

    private static Stream<DrgElement> collectDrgElements(AuthorityRequirement authorityRequirement) {
        return Stream.of(
                        authorityRequirement.getRequiredDecision(),
                        authorityRequirement.getRequiredInput(),
                        authorityRequirement.getRequiredAuthority())
                .filter(Objects::nonNull);
    }
}
