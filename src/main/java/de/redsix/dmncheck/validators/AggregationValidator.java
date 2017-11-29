package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.core.Validator;
import org.camunda.bpm.model.dmn.BuiltinAggregator;
import org.camunda.bpm.model.dmn.HitPolicy;
import org.camunda.bpm.model.dmn.instance.DecisionTable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public enum AggregationValidator implements Validator<DecisionTable> {
    instance;

    @Override
    public boolean isApplicable(DecisionTable decisionTable) {
        return !HitPolicy.COLLECT.equals(decisionTable.getHitPolicy());
    }

    @Override
    public List<ValidationResult> validate(DecisionTable decisionTable) {
        final BuiltinAggregator builtinAggregator = decisionTable.getAggregation();
        if (Objects.nonNull(builtinAggregator)) {
            return Collections.singletonList(ValidationResult.Builder.instance.with($ -> {
                $.message = "Aggregations are only valid with HitPolicy " + HitPolicy.COLLECT;
                $.element = decisionTable;
            }).build());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Class<DecisionTable> getClassUnderValidation() {
        return DecisionTable.class;
    }
}
