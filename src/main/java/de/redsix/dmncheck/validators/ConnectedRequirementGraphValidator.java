package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.core.RequirementGraphValidator;
import org.camunda.bpm.model.dmn.instance.Decision;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.DrgElement;
import org.camunda.bpm.model.dmn.instance.Input;
import org.camunda.bpm.model.dmn.instance.InputExpression;
import org.camunda.bpm.model.dmn.instance.OutputClause;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConnectedRequirementGraphValidator extends RequirementGraphValidator {

    @Override
    public List<ValidationResult> validate(DirectedAcyclicGraph<DrgElement, DefaultEdge> drg) {
        ConnectivityInspector<DrgElement, DefaultEdge> connectivityInspector = new ConnectivityInspector<>(drg);

        if (connectivityInspector.isConnected()) {
            return drg.edgeSet().stream()
                    .flatMap(edge -> checkInAndOuputs(drg.getEdgeSource(edge), drg.getEdgeTarget(edge)).stream())
                    .collect(Collectors.toList());
        } else {
            final List<Set<DrgElement>> connectedSetsOfSizeOne = connectivityInspector.connectedSets().stream()
                    .filter(connectedSet -> connectedSet.size() == 1)
                    .collect(Collectors.toList());

            if (connectedSetsOfSizeOne.isEmpty()) {
                final List<Set<DrgElement>> subgraphs = connectivityInspector.connectedSets().stream()
                        .filter(connectedSet -> connectedSet.size() > 1)
                        .collect(Collectors.toList());
                return Collections.singletonList(ValidationResult.init
                        .message("Found unconnected requirement graphs: " + subgraphs)
                        .element(subgraphs.iterator().next().iterator().next().getParentElement())
                        .build());
            } else {
                return connectedSetsOfSizeOne.stream().map(
                        connectedSetOfSizeOne -> ValidationResult.init
                                .message("Element is not connected to requirement graph")
                                .element(connectedSetOfSizeOne.iterator().next())
                                .build()
                ).collect(Collectors.toList());
            }
        }
    }

    private List<ValidationResult> checkInAndOuputs(DrgElement sourceElement, DrgElement targetElement) {
        if (sourceElement instanceof Decision && targetElement instanceof Decision) {
            final Decision sourceDecision = (Decision) sourceElement;
            final Decision targetDecision = (Decision) targetElement;

            return applyOnDecsionTable(sourceDecision, sourceDecisionTable ->
                    applyOnDecsionTable(targetDecision, targetDecisionTable -> {

                final Set<String> inputIds = sourceDecisionTable.getInputs().stream()
                        .map(Input::getInputExpression)
                        .map(InputExpression::getTextContent)
                        .collect(Collectors.toSet());

                final Set<String> outputIds = targetDecisionTable.getOutputs().stream()
                        .map(OutputClause::getName)
                        .collect(Collectors.toSet());

                inputIds.retainAll(outputIds);

                if (inputIds.isEmpty()) {
                    return Collections.singletonList(
                            ValidationResult.init
                                    .message("Inputs and outputs do not match in connected decisions.")
                                    .element(sourceDecision)
                                    .build());
                } else {
                    return Collections.emptyList();
                }
            }));
        } else {
            // We only validate in- and outputs for decisions as they are the only elements
            // with relevance in evaluation
            return Collections.emptyList();
        }
    }

    private List<ValidationResult> applyOnDecsionTable(Decision decision, Function<DecisionTable, List<ValidationResult>> validate) {
        final Collection<DecisionTable> decisionTables = decision.getChildElementsByType(DecisionTable.class);

        if (decisionTables.size() == 1) {
            return validate.apply(decisionTables.iterator().next());
        } else {
            return Collections.singletonList(ValidationResult.init
                    .message("There is either no or more than one decision table.")
                    .element(decision)
                    .build());
        }
    }
}
