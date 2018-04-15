package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.feel.FeelParser;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.util.Either;
import de.redsix.dmncheck.util.Eithers;
import de.redsix.dmncheck.util.Util;
import de.redsix.dmncheck.validators.core.SimpleValidator;
import org.camunda.bpm.model.dmn.HitPolicy;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.InputEntry;
import org.camunda.bpm.model.dmn.instance.Rule;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
public class ShadowedRuleValidator extends SimpleValidator<DecisionTable> {

    @Override
    public Class<DecisionTable> getClassUnderValidation() {
        return DecisionTable.class;
    }

    @Override
    public boolean isApplicable(DecisionTable decisionTable) {
        return !Arrays.asList(HitPolicy.COLLECT, HitPolicy.RULE_ORDER).contains(decisionTable.getHitPolicy());
    }

    @Override
    public List<ValidationResult> validate(DecisionTable decisionTable) {
        final ArrayList<Rule> rules = new ArrayList<>(decisionTable.getRules());
        Collections.reverse(rules);

        return Util.zip(IntStream.range(1, rules.size()).boxed(), rules.stream(),
                (n, rule) -> rules.stream().skip(n).flatMap(potentiallySubsumingRule -> {
                    final List<Either<Optional<Boolean>, ValidationResult.Builder>> subsumptionCheckResult = checkRulesForSubsumption(rule,
                            potentiallySubsumingRule);

                    final List<ValidationResult> parsingErrors = extractParsingErrors(rule, subsumptionCheckResult);

                    if (!parsingErrors.isEmpty()) {
                        return parsingErrors.stream();
                    }

                    final List<Optional<Boolean>> subsumptionResults = extractSubsumptionResults(subsumptionCheckResult);
                    if (subsumptionCheckIsPossible(subsumptionResults) && everythingIsSubsumed(subsumptionResults)) {
                        return Stream.of(ValidationResult.Builder.with($ -> {
                            $.message = "Rule is shadowed by rule " + potentiallySubsumingRule.getId();
                            $.element = rule;
                        }).build());
                    } else {
                        return Stream.empty();
                    }
                })).flatMap(Function.identity()).collect(Collectors.toList());
    }

    private List<Either<Optional<Boolean>, ValidationResult.Builder>> checkRulesForSubsumption(final Rule rule,
            final Rule potentiallySubsumingRule) {
        return Util
                .zip(rule.getInputEntries().stream(), potentiallySubsumingRule.getInputEntries().stream(), this::checkInputsForSubsumption)
                .collect(Collectors.toList());
    }

    private Either<Optional<Boolean>, ValidationResult.Builder> checkInputsForSubsumption(final InputEntry input,
            final InputEntry potentiallySubsumingInput) {
        return FeelParser.parse(input.getTextContent()).bind(inputExpression ->
                FeelParser.parse(potentiallySubsumingInput.getTextContent()).bind(potentiallySubsumingInputExpression ->
                        Eithers.left(potentiallySubsumingInputExpression.subsumes(inputExpression))));
    }

    private List<Optional<Boolean>> extractSubsumptionResults(
            final List<Either<Optional<Boolean>, ValidationResult.Builder>> subsumptionCheckResult) {
        return subsumptionCheckResult.stream()
                .map(Eithers::getLeft)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private List<ValidationResult> extractParsingErrors(final Rule rule,
            final List<Either<Optional<Boolean>, ValidationResult.Builder>> subsumptionCheckResult) {
        return subsumptionCheckResult.stream()
                .map(Eithers::getRight)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(builder -> builder.extend($ -> {
                    $.element = rule;
                }))
                .map(ValidationResult.Builder::build)
                .collect(Collectors.toList());
    }

    private boolean everythingIsSubsumed(final List<Optional<Boolean>> subsumptionResults) {
        return subsumptionResults.stream().allMatch(result -> result.orElse(false));
    }

    private boolean subsumptionCheckIsPossible(final List<Optional<Boolean>> subsumptionResults) {
        return !subsumptionResults.contains(Optional.<Boolean>empty());
    }
}
