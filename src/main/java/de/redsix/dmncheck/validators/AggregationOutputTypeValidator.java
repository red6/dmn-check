package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.model.ExpressionTypeEnum;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.core.GenericValidator;
import org.camunda.bpm.model.dmn.BuiltinAggregator;
import org.camunda.bpm.model.dmn.HitPolicy;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.Output;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public enum AggregationOutputTypeValidator implements GenericValidator<DecisionTable, Output> {
    instance;

    @Override
    public boolean isApplicable(DecisionTable decisionTable) {
        return Objects.nonNull(decisionTable.getAggregation()) &&
                Arrays.asList(BuiltinAggregator.MAX, BuiltinAggregator.MIN, BuiltinAggregator.SUM).contains(
                        decisionTable.getAggregation());
    }

    @Override
    public List<ValidationResult> validate(Output output) {
        if (!ExpressionTypeEnum.isNumeric(output.getTypeRef())) {
            return Collections.singletonList(ValidationResult.Builder.with($ -> {
                $.message = "Aggregations MAX, MIN, SUM are only valid with numeric output types";
                $.element = output;
            }).build());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Class<Output> getClassUnderValidation() {
        return Output.class;
    }

    @Override
    public Class<DecisionTable> getClassUsedToCheckApplicability() {
        return DecisionTable.class;
    }
}
