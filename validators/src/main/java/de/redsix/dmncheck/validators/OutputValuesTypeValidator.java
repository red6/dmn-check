package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.feel.ExpressionTypeParser;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.core.ValidationContext;
import org.camunda.bpm.model.dmn.instance.Output;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OutputValuesTypeValidator extends TypeValidator<Output> {

    @Override
    public boolean isApplicable(final Output output, ValidationContext validationContext) {
        final String expressionType = output.getTypeRef();
        return output.getOutputValues() != null
                && ExpressionTypeParser.parse(expressionType, validationContext.getItemDefinitions()).match(parseError -> false, parseResult -> true);
    }

    @Override
    public List<ValidationResult> validate(final Output output, ValidationContext validationContext) {
        final String expressionType = output.getTypeRef();

        return ExpressionTypeParser.parse(expressionType, validationContext.getItemDefinitions())
                .match(validationResult -> Collections.singletonList(validationResult.element(output).build()),
                       inputType -> typecheck(output,
                                Stream.of(output.getOutputValues()).map(toplevelExpressionLanguage::toExpression),
                                Stream.of(inputType)).collect(Collectors.toList()));
    }

    @Override
    protected Class<Output> getClassUnderValidation() {
        return Output.class;
    }

    @Override
    String errorMessage() {
        return "Type of predefined output values does not match type of output expression";
    }
}
