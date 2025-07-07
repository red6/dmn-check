package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.core.SimpleValidator;
import de.redsix.dmncheck.validators.core.ValidationContext;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.camunda.bpm.model.dmn.HitPolicy;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.Rule;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

public class ConflictingRuleValidator extends SimpleValidator<DecisionTable> {

    @Override
    public Class<DecisionTable> getClassUnderValidation() {
        return DecisionTable.class;
    }

    @Override
    public boolean isApplicable(
        DecisionTable decisionTable,
        ValidationContext validationContext
    ) {
        return true;
    }

    @Override
    public List<ValidationResult> validate(
        DecisionTable decisionTable,
        ValidationContext validationContext
    ) {
        return decisionTable
            .getRules()
            .stream()
            .collect(
                Collectors.groupingBy(
                    ConflictingRuleValidator::extractInputEntriesTextContent
                )
            )
            .values()
            .stream()
            .map(rules ->
                rules
                    .stream()
                    // Explicit types until https://github.com/typetools/checker-framework/issues/7064 is fixed.
                    .collect(
                        Collectors.<Rule, TreeSet<Rule>>toCollection(() ->
                            new TreeSet<>(
                                Comparator.comparing(
                                    ConflictingRuleValidator::extractInputAndOutputEntriesTextContent
                                )
                            )
                        )
                    )
            )
            .filter(rules -> rules.size() > 1)
            .map(rules ->
                ValidationResult.init
                    .message(
                        "Rule is conflicting with rules " +
                        rules.stream().skip(1).map(Rule::getId).toList()
                    )
                    .severity(
                        Arrays.asList(
                                HitPolicy.COLLECT,
                                HitPolicy.RULE_ORDER
                            ).contains(decisionTable.getHitPolicy())
                            ? Severity.WARNING
                            : Severity.ERROR
                    )
                    .element(rules.first())
                    .build()
            )
            .collect(Collectors.toList());
    }

    private static List<String> extractInputEntriesTextContent(Rule rule) {
        return rule
            .getInputEntries()
            .stream()
            .map(ModelElementInstance::getTextContent)
            .collect(Collectors.toList());
    }

    private static String extractInputAndOutputEntriesTextContent(Rule rule) {
        return Stream.concat(
            rule.getInputEntries().stream(),
            rule.getOutputEntries().stream()
        )
            .map(ModelElementInstance::getTextContent)
            .collect(Collectors.joining());
    }
}
