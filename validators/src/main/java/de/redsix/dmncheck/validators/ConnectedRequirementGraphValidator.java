package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.drg.RequirementGraph;
import de.redsix.dmncheck.feel.FeelParser;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.util.Either;
import de.redsix.dmncheck.util.TopLevelExpressionLanguage;
import de.redsix.dmncheck.validators.core.RequirementGraphValidator;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.instance.Decision;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.DrgElement;
import org.camunda.bpm.model.dmn.instance.Input;
import org.camunda.bpm.model.dmn.instance.LiteralExpression;
import org.camunda.bpm.model.dmn.instance.OutputClause;
import org.camunda.bpm.model.dmn.instance.Variable;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;

public class ConnectedRequirementGraphValidator extends RequirementGraphValidator {

    @Override
    public Optional<String> documentation() {
        return Optional.of("https://github.com/red6/dmn-check#correctly-connected-requirement-graphs");
    }

    private TopLevelExpressionLanguage toplevelExpressionLanguage = new TopLevelExpressionLanguage(null);

    @Override
    public List<ValidationResult> apply(final DmnModelInstance dmnModelInstance) {
        toplevelExpressionLanguage =
                new TopLevelExpressionLanguage(dmnModelInstance.getDefinitions().getExpressionLanguage());
        return super.apply(dmnModelInstance);
    }

    @Override
    public List<ValidationResult> validate(final RequirementGraph drg) {
        final ConnectivityInspector<DrgElement, DefaultEdge> connectivityInspector = new ConnectivityInspector<>(drg);

        if (connectivityInspector.isConnected()) {
            return drg.edgeSet().stream()
                    .flatMap(edge -> checkInAndOuputs(drg.getEdgeSource(edge), drg.getEdgeTarget(edge)).stream())
                    .toList();
        } else if (connectivityInspector.connectedSets().isEmpty()) {
            // Although an empty graph is not connected, we do not warn in this case as this is the responsibility of
            // another validator
            return Collections.emptyList();
        } else {
            return reportUnconnectedComponents(drg, connectivityInspector);
        }
    }

    private List<ValidationResult> checkInAndOuputs(final DrgElement sourceElement, final DrgElement targetElement) {
        if (sourceElement instanceof final Decision sourceDecision && targetElement instanceof final Decision targetDecision) {
            return checkInAndOutputs(sourceDecision, targetDecision);
        } else {
            // We only validate in- and outputs for decisions as they are the only elements
            // with relevance in evaluation
            return Collections.emptyList();
        }
    }

    private List<ValidationResult> checkInAndOutputs(final Decision sourceDecision, final Decision targetDecision) {
        return getOutputNames(sourceDecision)
                .bind(outputNames -> outputNames.isEmpty() && getLiteralExpression(sourceDecision).isPresent()
                        // A literal expression without a variable name is reported by the LiteralExpressionValidator
                        ? new Either.Right<>(true)
                        : usesAnyOf(targetDecision, outputNames))
                .match(List::of, matching -> {
                    if (matching) {
                        return Collections.emptyList();
                    } else {
                        return List.of(ValidationResult.init
                                .message("Inputs and outputs do not match in connected decisions.")
                                .element(sourceDecision)
                                .build());
                    }
                });
    }

    private Either<ValidationResult, Set<String>> getOutputNames(final Decision decision) {
        if (getLiteralExpression(decision).isPresent()) {
            return new Either.Right<>(Stream.ofNullable(decision.getVariable())
                    .map(Variable::getName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet()));
        }
        return getDecisionTable(decision).map(decisionTable -> decisionTable.getOutputs().stream()
                .map(OutputClause::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
    }

    private Either<ValidationResult, Boolean> usesAnyOf(final Decision decision, final Set<String> names) {
        final Optional<LiteralExpression> literalExpression = getLiteralExpression(decision);
        if (literalExpression.isPresent()) {
            // shortcut: plain text search, as the FEEL parser only supports unary tests and not full expressions
            final String text = Objects.requireNonNullElse(literalExpression.get().getTextContent(), "");
            return new Either.Right<>(names.stream()
                    .anyMatch(name -> Pattern.compile("(?<!\\w)" + Pattern.quote(name) + "(?!\\w)")
                            .matcher(text)
                            .find()));
        }
        return getDecisionTable(decision).bind(decisionTable -> decisionTable.getInputs().stream()
                .map(Input::getInputExpression)
                .map(toplevelExpressionLanguage::toExpression)
                .map(FeelParser::parse)
                .collect(Either.reduce())
                .match(
                        elementStep -> new Either.Left<>(elementStep.element(decision).build()),
                        inputExpressions -> new Either.Right<>(inputExpressions.stream()
                                .anyMatch(inputExpression -> names.stream().anyMatch(inputExpression::containsVariable)))));
    }

    private static Optional<LiteralExpression> getLiteralExpression(final Decision decision) {
        return decision.getChildElementsByType(LiteralExpression.class).stream().findFirst();
    }

    private static Either<ValidationResult, DecisionTable> getDecisionTable(final Decision decision) {
        final Collection<DecisionTable> decisionTables = decision.getChildElementsByType(DecisionTable.class);

        if (decisionTables.size() == 1) {
            return new Either.Right<>(decisionTables.iterator().next());
        } else {
            return new Either.Left<>(ValidationResult.init
                    .message("There is either no or more than one decision table.")
                    .element(decision)
                    .build());
        }
    }

    private List<ValidationResult> reportUnconnectedComponents(
            final RequirementGraph drg, final ConnectivityInspector<DrgElement, DefaultEdge> connectivityInspector) {
        final List<Set<DrgElement>> connectedSetsOfSizeOne = connectivityInspector.connectedSets().stream()
                .filter(connectedSet -> connectedSet.size() == 1)
                .toList();

        if (connectedSetsOfSizeOne.isEmpty()) {
            final List<Set<DrgElement>> subgraphs = connectivityInspector.connectedSets().stream()
                    .filter(connectedSet -> connectedSet.size() > 1)
                    .toList();
            return List.of(ValidationResult.init
                    .message("Found unconnected requirement graphs: " + subgraphs)
                    .element(drg.getDefinitions())
                    .build());
        } else {
            return connectedSetsOfSizeOne.stream()
                    .map(connectedSetOfSizeOne -> ValidationResult.init
                            .message("Element is not connected to requirement graph")
                            .element(connectedSetOfSizeOne.iterator().next())
                            .build())
                    .toList();
        }
    }
}
