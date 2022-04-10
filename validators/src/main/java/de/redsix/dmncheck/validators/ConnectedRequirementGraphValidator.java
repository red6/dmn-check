package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.drg.RequirementGraph;
import de.redsix.dmncheck.feel.FeelExpression;
import de.redsix.dmncheck.feel.FeelParser;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.util.Either;
import de.redsix.dmncheck.util.Expression;
import de.redsix.dmncheck.util.TopLevelExpressionLanguage;
import de.redsix.dmncheck.validators.core.RequirementGraphValidator;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.instance.Decision;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.DrgElement;
import org.camunda.bpm.model.dmn.instance.Input;
import org.camunda.bpm.model.dmn.instance.InputExpression;
import org.camunda.bpm.model.dmn.instance.OutputClause;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConnectedRequirementGraphValidator extends RequirementGraphValidator {

    private TopLevelExpressionLanguage toplevelExpressionLanguage = new TopLevelExpressionLanguage(null);

    @Override
    public List<ValidationResult> apply(final DmnModelInstance dmnModelInstance) {
        toplevelExpressionLanguage = new TopLevelExpressionLanguage(dmnModelInstance.getDefinitions().getExpressionLanguage());
        return super.apply(dmnModelInstance);
    }

    @Override
    public List<ValidationResult> validate(RequirementGraph drg) {
        ConnectivityInspector<DrgElement, DefaultEdge> connectivityInspector = new ConnectivityInspector<>(drg);

        if (connectivityInspector.isConnected()) {
            return drg.edgeSet().stream()
                    .flatMap(edge -> checkInAndOuputs(drg.getEdgeSource(edge), drg.getEdgeTarget(edge)).stream())
                    .collect(Collectors.toList());
        } else if (connectivityInspector.connectedSets().isEmpty()) {
            // Although an empty graph is not connected, we do not warn in this case as this is the responsibility of an other validator
            return Collections.emptyList();
        } else {
            return reportUnconnectedComponents(drg, connectivityInspector);
        }
    }

    private List<ValidationResult> checkInAndOuputs(DrgElement sourceElement, DrgElement targetElement) {
        if (sourceElement instanceof Decision && targetElement instanceof Decision) {
            final Decision sourceDecision = (Decision) sourceElement;
            final Decision targetDecision = (Decision) targetElement;

            return checkInAndOutputs(sourceDecision, targetDecision);
        } else {
            // We only validate in- and outputs for decisions as they are the only elements
            // with relevance in evaluation
            return Collections.emptyList();
        }
    }

    private List<ValidationResult> checkInAndOutputs(Decision sourceDecision, Decision targetDecision) {
        return applyOnDecsionTable(sourceDecision, sourceDecisionTable ->
                applyOnDecsionTable(targetDecision, targetDecisionTable -> {

            final Either<ValidationResult.Builder.ElementStep, List<FeelExpression>> eitherInputExpressions = targetDecisionTable.getInputs().stream()
                    .map(Input::getInputExpression)
                    .map(toplevelExpressionLanguage::toExpression)
                    .map(FeelParser::parse)
                    .collect(Either.reduce());

            Either<ValidationResult.Builder.ElementStep, Boolean> doInAndOutputsMatch = eitherInputExpressions.map(inputExpressions -> {
                final Set<String> outputIds = sourceDecisionTable.getOutputs().stream()
                        .map(OutputClause::getName)
                        .collect(Collectors.toSet());

                return inputExpressions.stream().anyMatch(inputExpression ->
                        outputIds.stream().anyMatch(inputExpression::containsVariable));
            });

            return doInAndOutputsMatch.match(elementStep -> Collections.singletonList(elementStep.element(targetDecision).build()), matching -> {
                if (matching) {
                    return Collections.emptyList();
                } else {
                    return Collections.singletonList(
                            ValidationResult.init
                                    .message("Inputs and outputs do not match in connected decisions.")
                                    .element(sourceDecision)
                                    .build());
                }
            });
        }));
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

    private List<ValidationResult> reportUnconnectedComponents(RequirementGraph drg, ConnectivityInspector<DrgElement, DefaultEdge> connectivityInspector) {
        final List<Set<DrgElement>> connectedSetsOfSizeOne = connectivityInspector.connectedSets().stream()
                .filter(connectedSet -> connectedSet.size() == 1)
                .collect(Collectors.toList());

        if (connectedSetsOfSizeOne.isEmpty()) {
            final List<Set<DrgElement>> subgraphs = connectivityInspector.connectedSets().stream()
                    .filter(connectedSet -> connectedSet.size() > 1)
                    .collect(Collectors.toList());
            return Collections.singletonList(ValidationResult.init
                    .message("Found unconnected requirement graphs: " + subgraphs)
                    .element(drg.getDefinitions())
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
