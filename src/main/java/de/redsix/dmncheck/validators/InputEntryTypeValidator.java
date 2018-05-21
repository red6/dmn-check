package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.feel.ExpressionType;
import de.redsix.dmncheck.feel.ExpressionTypeParser;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.util.Either;
import de.redsix.dmncheck.util.Eithers;
import de.redsix.dmncheck.util.Util;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.Input;
import org.camunda.bpm.model.dmn.instance.InputEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InputEntryTypeValidator extends TypeValidator {

    @Override
    public boolean isApplicable(DecisionTable decisionTable) {
        return decisionTable.getInputs().stream().allMatch(input -> {
            final String expressionType = input.getInputExpression().getTypeRef();
            return ExpressionTypeParser.parse(expressionType).match(parseResult -> true, parseError -> false);
        });
    }

    @Override
    public List<ValidationResult> validate(DecisionTable decisionTable) {

        return decisionTable.getRules().stream().flatMap(rule -> {
            final Stream<String> inputVariables = decisionTable.getInputs().stream().map(Input::getCamundaInputVariable);

            final Stream<InputEntry> inputExpressions = rule.getInputEntries().stream();

            final Either<List<ExpressionType>, ValidationResult.Builder.ElementStep> eitherInputTypes = decisionTable.getInputs().stream()
                    .map(input -> input.getInputExpression().getTypeRef())
                    .map(ExpressionTypeParser::parse)
                    .collect(Either.sequence());

            return eitherInputTypes.match(
                    inputTypes -> typecheck(rule, inputExpressions, inputVariables, inputTypes.stream()),
                    validationResult -> Stream.of(validationResult.element(rule).build()));
        }).collect(Collectors.toList());
    }

    @Override
    public String errorMessage() {
        return "Type of input entry does not match severity of input expression";
    }

    @Override
    boolean isEmptyAllowed() {
        return true;
    }
}
