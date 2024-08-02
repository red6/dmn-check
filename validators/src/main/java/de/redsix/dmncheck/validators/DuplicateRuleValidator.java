package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.core.SimpleValidator;
import de.redsix.dmncheck.validators.core.ValidationContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.camunda.bpm.model.dmn.HitPolicy;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.Rule;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

public class DuplicateRuleValidator extends SimpleValidator<DecisionTable> {

    @Override
    public Class<DecisionTable> getClassUnderValidation() {
        return DecisionTable.class;
    }

    @Override
    public boolean isApplicable(DecisionTable decisionTable, ValidationContext validationContext) {
        return true;
    }

    @Override
    public List<ValidationResult> validate(DecisionTable decisionTable, ValidationContext validationContext) {
        final Collection<Rule> rules = decisionTable.getRules();
        final List<List<String>> expressions = new ArrayList<>();
        final List<ValidationResult> result = new ArrayList<>();

        for (Rule rule : rules) {
            final List<String> rowElements = Stream.concat(
                            rule.getInputEntries().stream(), rule.getOutputEntries().stream())
                    .map(ModelElementInstance::getTextContent)
                    .collect(Collectors.toList());
            if (!expressions.contains(rowElements)) {
                expressions.add(rowElements);
            } else {
                result.add(ValidationResult.init
                        .message("Rule is defined more than once")
                        .severity(
                                HitPolicy.COLLECT.equals(decisionTable.getHitPolicy())
                                        ? Severity.WARNING
                                        : Severity.ERROR)
                        .element(rule)
                        .build());
            }
        }
        return result;
    }
}
