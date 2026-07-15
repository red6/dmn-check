package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.core.SimpleValidator;
import de.redsix.dmncheck.validators.core.ValidationContext;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.camunda.bpm.model.dmn.BuiltinAggregator;
import org.camunda.bpm.model.dmn.HitPolicy;
import org.camunda.bpm.model.dmn.instance.DecisionTable;

public class AggregationValidator extends SimpleValidator<DecisionTable> {

    @Override
    public boolean isApplicable(final DecisionTable decisionTable, final ValidationContext validationContext) {
        return !HitPolicy.COLLECT.equals(decisionTable.getHitPolicy());
    }

    @Override
    public List<ValidationResult> validate(final DecisionTable decisionTable, final ValidationContext validationContext) {
        final BuiltinAggregator builtinAggregator = decisionTable.getAggregation();
        if (Objects.nonNull(builtinAggregator)) {
            return List.of(ValidationResult.init
                    .message("Aggregations are only valid with HitPolicy " + HitPolicy.COLLECT)
                    .element(decisionTable)
                    .build());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Class<DecisionTable> getClassUnderValidation() {
        return DecisionTable.class;
    }
}
