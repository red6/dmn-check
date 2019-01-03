package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.feel.FeelParser;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.util.Either;
import de.redsix.dmncheck.util.Eithers;
import de.redsix.dmncheck.util.Util;
import de.redsix.dmncheck.validators.core.SimpleValidator;
import de.redsix.dmncheck.validators.core.ValidationContext;
import org.camunda.bpm.model.dmn.HitPolicy;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.InputEntry;
import org.camunda.bpm.model.dmn.instance.Rule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ShadowedRuleValidator extends SimpleValidator<DecisionTable> {

    @Override
    public Class<DecisionTable> getClassUnderValidation() {
        return DecisionTable.class;
    }

    @Override
    public boolean isApplicable(DecisionTable decisionTable, ValidationContext validationContext) {
        return !Arrays.asList(HitPolicy.COLLECT, HitPolicy.RULE_ORDER).contains(decisionTable.getHitPolicy());
    }

    @Override
    public List<ValidationResult> validate(DecisionTable decisionTable, ValidationContext validationContext) {
        final ArrayList<Rule> rules = new ArrayList<>(decisionTable.getRules());
        Collections.reverse(rules);

        return Util.zip(
                    IntStream.range(1, rules.size()).boxed(),
                    rules.stream(),
                    (n, rule) -> identifySubsumedRules(n, rule, rules))
                .flatMap(Function.identity()).collect(Collectors.toList());
    }

    private Stream<ValidationResult> identifySubsumedRules(final Integer n, final Rule rule, final ArrayList<Rule> rules) {
        return rules.stream()
                .skip(n)
                .flatMap(potentiallySubsumingRule -> collectSubsumptionResults(rule, potentiallySubsumingRule)
                        .match(validationResultElementStep -> Stream.of(validationResultElementStep.element(rule).build()),
                                subsumptionResults -> isRuleSubsumed(subsumptionResults, rule, potentiallySubsumingRule)));
    }

    private Either<ValidationResult.Builder.ElementStep, List<Optional<Boolean>>> collectSubsumptionResults(final Rule rule,
            final Rule potentiallySubsumingRule) {
        return Util
                .zip(rule.getInputEntries().stream(), potentiallySubsumingRule.getInputEntries().stream(), this::checkInputsForSubsumption)
                .collect(Either.reduce());
    }

    private Either<ValidationResult.Builder.ElementStep, Optional<Boolean>> checkInputsForSubsumption(final InputEntry input,
            final InputEntry potentiallySubsumingInput) {
        return FeelParser.parse(input.getTextContent()).bind(inputExpression ->
                FeelParser.parse(potentiallySubsumingInput.getTextContent()).bind(potentiallySubsumingInputExpression ->
                        Eithers.right(potentiallySubsumingInputExpression.subsumes(inputExpression))));
    }

    private Stream<ValidationResult> isRuleSubsumed(final List<Optional<Boolean>> subsumptionResults, final Rule rule,
            final Rule potentiallySubsumingRule) {
        if (subsumptionCheckIsPossible(subsumptionResults) && everythingIsSubsumed(subsumptionResults)) {
            return Stream.of(ValidationResult.init.message("Rule is shadowed by rule " + potentiallySubsumingRule.getId()).element(rule)
                    .build());
        } else {
            return Stream.empty();
        }
    }

    private boolean everythingIsSubsumed(final List<Optional<Boolean>> subsumptionResults) {
        return subsumptionResults.stream().allMatch(result -> result.orElse(false));
    }

    private boolean subsumptionCheckIsPossible(final List<Optional<Boolean>> subsumptionResults) {
        return !subsumptionResults.contains(Optional.<Boolean>empty());
    }
}
