package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.core.SimpleValidator;
import de.redsix.dmncheck.validators.core.ValidationContext;
import org.camunda.bpm.model.dmn.HitPolicy;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.Rule;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConflictingRuleValidator extends SimpleValidator<DecisionTable> {

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
        return decisionTable.getRules().stream()
                        .collect(Collectors.groupingBy(ConflictingRuleValidator::extractInputEntriesTextContent)).values().stream()
                        .map(rules -> rules.stream().collect(Collectors.toCollection(() -> new TreeSet<>(
                                    Comparator.comparing(ConflictingRuleValidator::extractInputAndOutputEntriesTextContent)))))
                            .filter(rules -> rules.size() > 1)
                            .map(rules -> ValidationResult.init
                        .message("Rule is conflicting with rules " + rules.stream().skip(1).map(Rule::getId).collect(Collectors.toList()))
                        .element(rules.first())
                        .build())
                            .collect(Collectors.toList());
    }

    private static List<String> extractInputEntriesTextContent(Rule rule) {
        return rule.getInputEntries().stream().map(ModelElementInstance::getTextContent).collect(Collectors.toList());
    }

    private static String extractInputAndOutputEntriesTextContent(Rule rule) {
        return Stream.concat(rule.getInputEntries().stream(), rule.getOutputEntries().stream()).map(ModelElementInstance::getTextContent)
                .collect(Collectors.joining());
    }
}
