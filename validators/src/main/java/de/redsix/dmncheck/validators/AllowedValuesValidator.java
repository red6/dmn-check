package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.feel.FeelExpression;
import de.redsix.dmncheck.feel.FeelParser;
import de.redsix.dmncheck.feel.Operator;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.util.Expression;
import de.redsix.dmncheck.util.TopLevelExpressionLanguage;
import de.redsix.dmncheck.validators.core.SimpleValidator;
import de.redsix.dmncheck.validators.core.ValidationContext;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.ItemDefinition;
import org.camunda.bpm.model.dmn.instance.Rule;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Checks that input and output entries only use the allowed values of the item definition of their column. */
public class AllowedValuesValidator extends SimpleValidator<DecisionTable> {

    private TopLevelExpressionLanguage toplevelExpressionLanguage = new TopLevelExpressionLanguage(null);

    @Override
    public List<ValidationResult> apply(final DmnModelInstance dmnModelInstance) {
        toplevelExpressionLanguage =
                new TopLevelExpressionLanguage(dmnModelInstance.getDefinitions().getExpressionLanguage());
        return super.apply(dmnModelInstance);
    }

    @Override
    public Class<DecisionTable> getClassUnderValidation() {
        return DecisionTable.class;
    }

    @Override
    public boolean isApplicable(final DecisionTable decisionTable, final ValidationContext validationContext) {
        return true;
    }

    @Override
    public List<ValidationResult> validate(final DecisionTable decisionTable, final ValidationContext validationContext) {
        final Collection<ItemDefinition> itemDefinitions = validationContext.getItemDefinitions();
        final List<Optional<ItemDefinition>> inputItemDefinitions = decisionTable.getInputs().stream()
                .map(input -> findItemDefinitionWithAllowedValues(
                        input.getInputExpression().getTypeRef(), itemDefinitions))
                .toList();
        final List<Optional<ItemDefinition>> outputItemDefinitions = decisionTable.getOutputs().stream()
                .map(output -> findItemDefinitionWithAllowedValues(output.getTypeRef(), itemDefinitions))
                .toList();

        return decisionTable.getRules().stream()
                .flatMap(rule -> Stream.concat(
                        checkEntries(
                                rule,
                                rule.getInputEntries().stream()
                                        .map(toplevelExpressionLanguage::toExpression)
                                        .toList(),
                                inputItemDefinitions),
                        checkEntries(
                                rule,
                                rule.getOutputEntries().stream()
                                        .map(toplevelExpressionLanguage::toExpression)
                                        .toList(),
                                outputItemDefinitions)))
                .toList();
    }

    private static Optional<ItemDefinition> findItemDefinitionWithAllowedValues(
            final @Nullable String typeRef, final Collection<ItemDefinition> itemDefinitions) {
        return itemDefinitions.stream()
                .filter(itemDefinition -> itemDefinition.getName() != null
                        && itemDefinition.getName().equals(typeRef)
                        && itemDefinition.getAllowedValues() != null)
                .findFirst();
    }

    private Stream<ValidationResult> checkEntries(
            final Rule rule, final List<Expression> entries, final List<Optional<ItemDefinition>> itemDefinitions) {
        return IntStream.range(0, Math.min(entries.size(), itemDefinitions.size()))
                .boxed()
                .flatMap(i -> itemDefinitions.get(i).stream()
                        .flatMap(itemDefinition -> checkEntry(rule, entries.get(i), itemDefinition)));
    }

    private Stream<ValidationResult> checkEntry(
            final Rule rule, final Expression entry, final ItemDefinition itemDefinition) {
        // Parsing errors are already reported by the type validators
        return FeelParser.parse(entry)
                .bind(entryExpression -> FeelParser.parse(
                                toplevelExpressionLanguage.toExpression(itemDefinition.getAllowedValues()))
                        .map(allowedValues -> Stream.of(entryExpression)
                                .flatMap(AllowedValuesValidator::flatten)
                                .filter(value -> isAllowed(value, allowedValues).equals(Optional.of(false)))
                                .map(value -> ValidationResult.init
                                        .message("Value " + entry.textContent.strip() + " is not allowed by item definition "
                                                + itemDefinition.getName())
                                        .element(rule)
                                        .build())
                                .limit(1)))
                .match(__ -> Stream.empty(), results -> results);
    }

    /**
     * Only literals are checked, since ranges and comparisons are allowed to cover more than the allowed values, e.g.,
     * {@code > 5} for allowed values {@code 6, 7}.
     *
     * @return {@code Optional.empty()} if this cannot be decided
     */
    private static Optional<Boolean> isAllowed(final FeelExpression value, final FeelExpression allowedValues) {
        if (!isValueLiteral(value)) {
            return Optional.empty();
        }

        final List<Optional<Boolean>> results = flatten(allowedValues)
                .map(allowedValue -> isAllowedBy(allowedValue, value))
                .toList();

        if (results.contains(Optional.of(true))) {
            return Optional.of(true);
        }
        return results.contains(Optional.<Boolean>empty()) ? Optional.empty() : Optional.of(false);
    }

    private static Stream<FeelExpression> flatten(final FeelExpression expression) {
        return switch (expression) {
            case FeelExpression.DisjunctionExpression(final var head, final var tail) ->
                    Stream.concat(flatten(head), flatten(tail));
            default -> Stream.of(expression);
        };
    }

    /**
     * The subsumption check can neither compare e.g. an integer with a double bound nor decide comparisons like
     * {@code < 10} or negations of strings, so those cases are decided here or left undecided.
     */
    private static Optional<Boolean> isAllowedBy(final FeelExpression allowedValue, final FeelExpression value) {
        if (!literalClasses(allowedValue).allMatch(value.getClass()::equals)) {
            return Optional.empty();
        }
        return switch (allowedValue) {
            case FeelExpression.NaryExpression(final var operator, final var operands)
                    when operator == Operator.NOT && operands.size() == 1 && isValueLiteral(operands.getFirst()) ->
                    Optional.of(!operands.getFirst().equals(value));
            // Subsumption does not treat strings as literals and would wrongly reject strings within the range
            case FeelExpression.RangeExpression(final var __, final var ___, final var ____, final var _____) ->
                    value instanceof FeelExpression.StringLiteral ? Optional.empty() : allowedValue.subsumes(value);
            default -> isValueLiteral(allowedValue) ? Optional.of(allowedValue.equals(value)) : Optional.empty();
        };
    }

    private static Stream<Class<?>> literalClasses(final FeelExpression expression) {
        return switch (expression) {
            case FeelExpression.RangeExpression(final var __, final var lowerBound, final var upperBound, final var ___) ->
                    Stream.concat(literalClasses(lowerBound), literalClasses(upperBound));
            case FeelExpression.NaryExpression(final var __, final var operands) ->
                    operands.stream().flatMap(AllowedValuesValidator::literalClasses);
            default -> Stream.of(expression.getClass());
        };
    }

    private static boolean isValueLiteral(final FeelExpression expression) {
        return switch (expression) {
            case FeelExpression.BooleanLiteral(final var __) -> true;
            case FeelExpression.IntegerLiteral(final var __) -> true;
            case FeelExpression.DoubleLiteral(final var __) -> true;
            case FeelExpression.StringLiteral(final var __) -> true;
            case FeelExpression.DateLiteral(final var __) -> true;
            case FeelExpression.DateTimeLiteral(final var __) -> true;
            default -> false;
        };
    }
}
