package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.feel.ExpressionType;
import de.redsix.dmncheck.feel.ExpressionTypeParser;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.util.Either;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.OutputClause;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OutputEntryTypeValidator extends TypeValidator<DecisionTable> {

    @Override
    public boolean isApplicable(DecisionTable decisionTable) {
        return decisionTable.getOutputs().stream().allMatch(output -> {
            final String expressionType = output.getTypeRef();
            return ExpressionTypeParser.parse(expressionType).match(parseError -> false, parseResult -> true);
        });
    }

    @Override
    public List<ValidationResult> validate(DecisionTable decisionTable) {
        final Either<ValidationResult.Builder.ElementStep, List<ExpressionType>> eitherOutputTypes = decisionTable.getOutputs().stream()
                .map(OutputClause::getTypeRef)
                .map(ExpressionTypeParser::parse)
                .collect(Either.reduce());

        return decisionTable.getRules().stream().flatMap(rule ->
                eitherOutputTypes.match(
                        validationResult -> Stream.of(validationResult.element(rule).build()),
                        outputTypes -> typecheck(rule, rule.getOutputEntries().stream(), outputTypes.stream())))
                .collect(Collectors.toList());
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
