package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.feel.ExpressionType;
import de.redsix.dmncheck.result.ValidationResult;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.OutputClause;
import org.camunda.bpm.model.dmn.instance.OutputEntry;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OutputEntryTypeValidator extends TypeValidator {

    @Override
    public boolean isApplicable(DecisionTable decisionTable) {
        return decisionTable.getOutputs().stream().allMatch(output -> {
            final String expressionType = output.getTypeRef();
            return Objects.isNull(expressionType) || ExpressionType.isValid(expressionType);
        });
    }

    @Override
    public List<ValidationResult> validate(DecisionTable decisionTable) {
        return decisionTable.getRules().stream().flatMap(rule -> {
            final Stream<OutputEntry> outputEntry = rule.getOutputEntries().stream();

            final Stream<ExpressionType> outputTypes = decisionTable.getOutputs().stream()
                    .map(OutputClause::getTypeRef)
                    .map(Optional::ofNullable)
                    .map(ExpressionType::getType);

            return typecheck(rule, outputEntry, outputTypes);
        }).collect(Collectors.toList());
    }

    @Override
    public String errorMessage() {
        return "Type of output entry does not match severity of output expression";
    }

    @Override
    boolean isEmptyAllowed() {
        return false;
    }
}
