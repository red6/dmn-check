package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.model.ExpressionType;
import de.redsix.dmncheck.result.ValidationResult;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.OutputClause;
import org.camunda.bpm.model.dmn.instance.OutputEntry;

import javax.management.openmbean.OpenDataException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum  OutputEntryTypeValidator implements TypeValidator {
    instance;

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

            final Stream<Optional<ExpressionType>> outputTypes = decisionTable.getOutputs().stream()
                    .map(OutputClause::getTypeRef)
                    .map(typeRef -> Optional.ofNullable(typeRef).map(String::toUpperCase).map(ExpressionType::valueOf));

            return typecheck(rule, outputEntry, outputTypes);
        }).collect(Collectors.toList());
    }

    @Override
    public boolean isEmptyAllowed() {
        return false;
    }

    @Override
    public String errorMessage() {
        return "Type of output entry does not match type of output expression";
    }
}
