package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.feel.ExpressionType;
import de.redsix.dmncheck.feel.ExpressionTypeParser;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.util.Either;
import de.redsix.dmncheck.validators.core.GenericValidator;
import org.camunda.bpm.model.dmn.BuiltinAggregator;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.Output;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class AggregationOutputTypeValidator extends GenericValidator<DecisionTable, Output> {

    @Override
    public boolean isApplicable(DecisionTable decisionTable) {
        return Objects.nonNull(decisionTable.getAggregation()) &&
                Arrays.asList(BuiltinAggregator.MAX, BuiltinAggregator.MIN, BuiltinAggregator.SUM).contains(
                        decisionTable.getAggregation());
    }

    @Override
    public List<ValidationResult> validate(Output output) {
        if (output.getTypeRef() == null) {
            return Collections.singletonList(ValidationResult.init
                    .message("An aggregation is used but no output severity is defined")
                    .severity(Severity.WARNING)
                    .element(output)
                    .build());
        } else {
            final Either<ExpressionType, ValidationResult.Builder.ElementStep> eitherType = ExpressionTypeParser.parse(output.getTypeRef());
            return eitherType.match(type -> {
                if (!ExpressionType.isNumeric(type)) {
                    return Collections.singletonList(
                            ValidationResult.init.message("Aggregations MAX, MIN, SUM are only valid with numeric output types")
                                    .element(output).build());
                } else {
                    return Collections.emptyList();
                }
            }, validationResult -> Collections.singletonList(validationResult.element(output).build()));
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
