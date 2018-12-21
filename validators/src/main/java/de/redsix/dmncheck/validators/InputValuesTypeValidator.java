package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.feel.ExpressionTypeParser;
import de.redsix.dmncheck.result.ValidationResult;
import org.camunda.bpm.model.dmn.instance.Input;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InputValuesTypeValidator extends TypeValidator<Input> {

    @Override
    public boolean isApplicable(final Input input) {
        final String expressionType = input.getInputExpression().getTypeRef();
        return input.getInputValues() != null
                && ExpressionTypeParser.parse(expressionType).match(parseError -> false, parseResult -> true);
    }

    @Override
    public List<ValidationResult> validate(final Input input) {
        final String expressionType = input.getInputExpression().getTypeRef();

        return ExpressionTypeParser.parse(expressionType)
                .match(validationResult -> Collections.singletonList(validationResult.element(input).build()),
                        inputType -> typecheck(input, Stream.of(input.getInputValues()), Stream.of(inputType))
                                .collect(Collectors.toList()));
    }

    @Override
    protected Class<Input> getClassUnderValidation() {
        return Input.class;
    }

    @Override
    String errorMessage() {
        return "Type of predefined input values does not match type of input expression";
    }
}
