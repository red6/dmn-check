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
import org.camunda.bpm.model.dmn.instance.OutputClause;

public class OutputEntryTypeValidator extends TypeValidator<DecisionTable> {

    @Override
    public boolean isApplicable(
        DecisionTable decisionTable,
        ValidationContext validationContext
    ) {
        return decisionTable
            .getOutputs()
            .stream()
            .allMatch(output -> {
                final String expressionType = output.getTypeRef();
                return ExpressionTypeParser.parse(
                    expressionType,
                    validationContext.getItemDefinitions()
                ).match(parseError -> false, parseResult -> true);
            });
    }

    @Override
    public List<ValidationResult> validate(
        DecisionTable decisionTable,
        ValidationContext validationContext
    ) {
        final Either<
            ValidationResult.Builder.ElementStep,
            List<ExpressionType>
        > eitherOutputTypes = decisionTable
            .getOutputs()
            .stream()
            .map(OutputClause::getTypeRef)
            .map(typeRef ->
                ExpressionTypeParser.parse(
                    typeRef,
                    validationContext.getItemDefinitions()
                )
            )
            .collect(Either.reduce());

        return decisionTable
            .getRules()
            .stream()
            .flatMap(rule ->
                eitherOutputTypes.match(
                    validationResult ->
                        Stream.of(validationResult.element(rule).build()),
                    outputTypes ->
                        typecheck(
                            rule,
                            rule
                                .getOutputEntries()
                                .stream()
                                .map(toplevelExpressionLanguage::toExpression),
                            outputTypes.stream()
                        )
                )
            )
            .toList();
    }

    @Override
    protected Class<DecisionTable> getClassUnderValidation() {
        return DecisionTable.class;
    }

    @Override
    public String errorMessage() {
        return "Type of output entry does not match type of output expression";
    }
}
