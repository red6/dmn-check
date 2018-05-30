package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.feel.ExpressionType;
import de.redsix.dmncheck.feel.ExpressionTypeParser;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.util.Either;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.OutputClause;
import org.camunda.bpm.model.dmn.instance.OutputEntry;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OutputEntryTypeValidator extends TypeValidator {

    @Override
    public boolean isApplicable(DecisionTable decisionTable) {
        return decisionTable.getOutputs().stream().allMatch(output -> {
            final String expressionType = output.getTypeRef();
            return ExpressionTypeParser.parse(expressionType).match(parseResult -> true, parseError -> false);
        });
    }

    @Override
    public List<ValidationResult> validate(DecisionTable decisionTable) {
        return decisionTable.getRules().stream().flatMap(rule -> {
            final Stream<OutputEntry> outputEntry = rule.getOutputEntries().stream();

            final Either<List<ExpressionType>, ValidationResult.Builder.ElementStep> eitherOutputTypes = decisionTable.getOutputs().stream()
                    .map(OutputClause::getTypeRef)
                    .map(ExpressionTypeParser::parse)
                    .collect(Either.sequence());

            return eitherOutputTypes.match(
                    outputTypes -> typecheck(rule, outputEntry, outputTypes.stream()),
                    validationResult -> Stream.of(validationResult.element(rule).build()));
        }).collect(Collectors.toList());
    }

    @Override
    public String errorMessage() {
        return "Type of output entry does not match type of output expression";
    }

    @Override
    boolean isEmptyAllowed() {
        return false;
    }
}
