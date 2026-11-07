package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.feel.FeelParser;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.util.Either;
import de.redsix.dmncheck.util.TopLevelExpressionLanguage;
import de.redsix.dmncheck.util.Util;
import de.redsix.dmncheck.validators.core.SimpleValidator;
import de.redsix.dmncheck.validators.core.ValidationContext;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.HitPolicy;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.InputEntry;
import org.camunda.bpm.model.dmn.instance.Rule;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

/** Hit policy {@code UNIQUE} requires that the rules of a decision table do not overlap. */
public class OverlappingRuleValidator extends SimpleValidator<DecisionTable> {

    @Override
    public Optional<String> documentation() {
        return Optional.of("https://github.com/red6/dmn-check#overlapping-rules");
    }

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
        return HitPolicy.UNIQUE.equals(decisionTable.getHitPolicy());
    }

    @Override
    public List<ValidationResult> validate(final DecisionTable decisionTable, final ValidationContext validationContext) {
        final List<Rule> rules = List.copyOf(decisionTable.getRules());

        return IntStream.range(0, rules.size())
                .boxed()
                .flatMap(i -> rules.stream().skip(i + 1L).flatMap(otherRule -> checkRules(rules.get(i), otherRule)))
                .toList();
    }

    private Stream<ValidationResult> checkRules(final Rule rule, final Rule otherRule) {
        // Rules with identical inputs are already reported by DuplicateRuleValidator and ConflictingRuleValidator
        if (inputTextContent(rule).equals(inputTextContent(otherRule))) {
            return Stream.empty();
        }

        return Util.zip(rule.getInputEntries().stream(), otherRule.getInputEntries().stream(), this::checkInputs)
                .collect(Either.reduce())
                .match(
                        // Parsing errors are already reported by ShadowedRuleValidator and InputEntryTypeValidator
                        validationResultElementStep -> Stream.<ValidationResult>empty(),
                        overlapResults -> overlapResults.stream().allMatch(result -> result.orElse(false))
                                ? Stream.of(ValidationResult.init
                                        .message("Rule overlaps with rule " + otherRule.getId()
                                                + ", but hit policy UNIQUE requires disjoint rules")
                                        .element(rule)
                                        .build())
                                : Stream.empty());
    }

    private Either<ValidationResult.Builder.ElementStep, Optional<Boolean>> checkInputs(
            final InputEntry input, final InputEntry otherInput) {
        return FeelParser.parse(toplevelExpressionLanguage.toExpression(input))
                .bind(inputExpression -> FeelParser.parse(toplevelExpressionLanguage.toExpression(otherInput))
                        .bind(otherInputExpression ->
                                new Either.Right<>(inputExpression.overlaps(otherInputExpression))));
    }

    private static List<String> inputTextContent(final Rule rule) {
        return rule.getInputEntries().stream()
                .map(ModelElementInstance::getTextContent)
                .toList();
    }
}
