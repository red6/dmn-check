package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.model.ExpressionType;
import de.redsix.dmncheck.result.ValidationResult;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.Input;
import org.camunda.bpm.model.dmn.instance.InputEntry;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum InputEntryTypeValidator implements TypeValidator {
    instance;

    @Override
    public boolean isApplicable(DecisionTable decisionTable) {
        return decisionTable.getInputs().stream().allMatch(input -> {
            final String expressionType = input.getInputExpression().getTypeRef();
            return Objects.nonNull(expressionType) && ExpressionType.isValid(expressionType);
        });
    }

    @Override
    public List<ValidationResult> validate(DecisionTable decisionTable) {

        return decisionTable.getRules().stream().flatMap(rule -> {
            final Stream<String> inputVariables = decisionTable.getInputs().stream().map(Input::getCamundaInputVariable);

            final Stream<InputEntry> inputExpressions = rule.getInputEntries().stream();

            final Stream<ExpressionType> inputTypes = decisionTable.getInputs().stream().map(
                    input -> input.getInputExpression().getTypeRef()).map(String::toUpperCase).map(
                    ExpressionType::valueOf);

            return typecheck(rule, inputExpressions, inputVariables, inputTypes);
        }).collect(Collectors.toList());
    }

    @Override
    public boolean isEmptyAllowed() {
        return true;
    }

    @Override
    public String errorMessage() {
        return "Type of input entry does not match type of input expression";
    }
}
