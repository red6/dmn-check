package de.redsix.dmncheck.validators;

import org.camunda.bpm.model.dmn.HitPolicy;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.Rule;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public enum DuplicateRuleValidator implements Validator<DecisionTable> {
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
        final Collection<Rule> rules = decisionTable.getRules();
        final List<List<String>> expressions = new ArrayList<>();
        final List<ValidationResult> result = new ArrayList<>();

        for (Rule rule : rules) {
            final List<String> rowElements = rule.getInputEntries().stream().
                    map(ModelElementInstance::getTextContent).collect(Collectors.toList());
            if (!expressions.contains(rowElements)) {
                expressions.add(rowElements);
            } else {
                result.add(ValidationResult.from("Rule is defined more than once " + rowElements));
            }
        }
        return result;
    }
}
