package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.feel.FeelParser;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.util.Either;
import de.redsix.dmncheck.util.Eithers;
import de.redsix.dmncheck.util.Util;
import de.redsix.dmncheck.validators.core.Validator;
import org.camunda.bpm.model.dmn.HitPolicy;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
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

public enum ShadowedRuleValidator implements Validator<DecisionTable> {
    instance;

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
                    final List<Either<Optional<Boolean>, ValidationResult.Builder>> foo = Util
                            .zip(rule.getInputEntries().stream(), potentiallySubsumingRule.getInputEntries().stream(),
                                    (input, potentiallySubsumingInput) -> FeelParser.parse(input.getTextContent())
                                            .bind(inputExpression -> FeelParser.parse(potentiallySubsumingInput.getTextContent())
                                                    .bind(potentiallySubsumingInputExpression -> Eithers
                                                            .left(potentiallySubsumingInputExpression.subsumes(inputExpression)))))
                            .collect(Collectors.toList());

                    final List<ValidationResult> parsingErrors = foo.stream().map(Eithers::getRight).filter(Optional::isPresent)
                            .map(Optional::get).map(builder -> builder.extend($ -> {
                                $.element = rule;
                            })).map(ValidationResult.Builder::build).collect(Collectors.toList());

                    if (parsingErrors.isEmpty()) {
                        final List<Optional<Boolean>> subsumptionResults = foo.stream().map(Eithers::getLeft).filter(Optional::isPresent)
                                .map(Optional::get).collect(Collectors.toList());
                        if (!subsumptionResults.contains(Optional.<Boolean>empty()) && subsumptionResults.stream()
                                .allMatch(result -> result.orElse(false))) {
                            return Stream.of(ValidationResult.Builder.with($ -> {
                                $.message = "Rule is shadowed by rule " + potentiallySubsumingRule.getId();
                                $.element = rule;
                            }).build());
                        } else {
                            return Stream.empty();
                        }
                    } else {
                        return parsingErrors.stream();
                    }
                })).flatMap(Function.identity()).collect(Collectors.toList());
    }
}
