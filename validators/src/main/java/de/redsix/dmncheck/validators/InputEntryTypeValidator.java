package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.feel.ExpressionType;
import de.redsix.dmncheck.feel.ExpressionTypeParser;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.util.Either;
import de.redsix.dmncheck.validators.core.ValidationContext;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.Input;

public class InputEntryTypeValidator extends TypeValidator<DecisionTable> {

    @Override
    public boolean isApplicable(DecisionTable decisionTable, ValidationContext validationContext) {
        return decisionTable.getInputs().stream().allMatch(input -> {
            final String expressionType = input.getInputExpression().getTypeRef();
            return ExpressionTypeParser.parse(expressionType, validationContext.getItemDefinitions())
                    .match(parseError -> false, parseResult -> true);
        });
    }

    @Override
    public List<ValidationResult> validate(DecisionTable decisionTable, ValidationContext validationContext) {
        final Either<ValidationResult.Builder.ElementStep, List<ExpressionType>> eitherInputTypes =
                decisionTable.getInputs().stream()
                        .map(input -> input.getInputExpression().getTypeRef())
                        .map(typeRef -> ExpressionTypeParser.parse(typeRef, validationContext.getItemDefinitions()))
                        .collect(Either.reduce());

        return decisionTable.getRules().stream()
                .flatMap(rule -> {
                    final Stream<String> inputVariables =
                            decisionTable.getInputs().stream().map(Input::getCamundaInputVariable);

                    return eitherInputTypes.match(
                            validationResult ->
                                    Stream.of(validationResult.element(rule).build()),
                            inputTypes -> typecheck(
                                    rule,
                                    rule.getInputEntries().stream().map(toplevelExpressionLanguage::toExpression),
                                    inputVariables,
                                    inputTypes.stream()));
                })
                .collect(Collectors.toList());
    }

    @Override
    protected Class<DecisionTable> getClassUnderValidation() {
        return DecisionTable.class;
    }

    @Override
    public String errorMessage() {
        return "Type of input entry does not match type of input expression";
    }
}
