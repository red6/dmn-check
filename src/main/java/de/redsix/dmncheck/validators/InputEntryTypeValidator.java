package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.feel.ExpressionType;
import de.redsix.dmncheck.result.ValidationResult;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.Input;
import org.camunda.bpm.model.dmn.instance.InputEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
public class InputEntryTypeValidator extends TypeValidator {

    @Override
    public boolean isApplicable(DecisionTable decisionTable) {
        return decisionTable.getInputs().stream().allMatch(input -> {
            final String expressionType = input.getInputExpression().getTypeRef();
            return Objects.isNull(expressionType) || ExpressionType.isValid(expressionType);
        });
    }

    @Override
    public List<ValidationResult> validate(DecisionTable decisionTable) {

        return decisionTable.getRules().stream().flatMap(rule -> {
            final Stream<String> inputVariables = decisionTable.getInputs().stream().map(Input::getCamundaInputVariable);

            final Stream<InputEntry> inputExpressions = rule.getInputEntries().stream();

            final Stream<ExpressionType> inputTypes = decisionTable.getInputs().stream()
                    .map(input -> input.getInputExpression().getTypeRef())
                    .map(Optional::ofNullable)
                    .map(ExpressionType::getType);

            return typecheck(rule, inputExpressions, inputVariables, inputTypes);
        }).collect(Collectors.toList());
    }

    @Override
    public String errorMessage() {
        return "Type of input entry does not match type of input expression";
    }

    @Override
    boolean isEmptyAllowed() {
        return true;
    }
}
