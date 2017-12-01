package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.core.Validator;
import org.camunda.bpm.model.dmn.HitPolicy;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.Rule;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ConflictingRuleValidator implements Validator<DecisionTable> {
    instance;

    @Override
    public Class<DecisionTable> getClassUnderValidation() {
        return DecisionTable.class;
    }

    @Override
    public boolean isApplicable(DecisionTable decisionTable) {
        return !HitPolicy.COLLECT.equals(decisionTable.getHitPolicy());
    }

    @Override
    public List<ValidationResult> validate(DecisionTable decisionTable) {
        return decisionTable.getRules().stream()
                .collect(Collectors.groupingBy(ConflictingRuleValidator::extractInputEntriesTextContent)).entrySet().stream()
                .map(entry -> entry.getValue().stream().collect(Collectors.toCollection(
                        () -> new TreeSet<>(Comparator.comparing(ConflictingRuleValidator::extractInputAndOutputEntriesTextContent)))))
                .filter(rules -> rules.size() > 1)
                .map(rules -> ValidationResult.Builder.with($ -> {
                    $.message = "Rule is conflicting with rules " + rules.stream().skip(1).map(Rule::getId).collect(Collectors.toList());
                    $.element = rules.first();
                }).build()).collect(Collectors.toList());
    }

    private static List<String> extractInputEntriesTextContent(Rule rule) {
        return rule.getInputEntries().stream().map(ModelElementInstance::getTextContent).collect(Collectors.toList());
    }

    private static String extractInputAndOutputEntriesTextContent(Rule rule) {
        return Stream.concat(rule.getInputEntries().stream(), rule.getOutputEntries().stream()).map(ModelElementInstance::getTextContent)
                .collect(Collectors.joining());
    }
}
