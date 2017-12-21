package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.feel.FeelParser;
import de.redsix.dmncheck.feel.FeelTypecheck;
import de.redsix.dmncheck.model.ExpressionType;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.util.Either;
import de.redsix.dmncheck.util.Eithers;
import de.redsix.dmncheck.util.Util;
import de.redsix.dmncheck.validators.core.Validator;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.InputEntry;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum InputEntryTypeValidator implements Validator<DecisionTable> {
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
            final Stream<InputEntry> inputExpressions = rule.getInputEntries().stream();

            final Stream<ExpressionType> inputTypes = decisionTable.getInputs().stream().map(
                    input -> input.getInputExpression().getTypeRef()).map(String::toUpperCase).map(
                    ExpressionType::valueOf);

            return Util.zip(inputExpressions, inputTypes, (inputEntry, expectedType) -> {
                final Either<ExpressionType, ValidationResult.Builder> typedcheckResult = FeelTypecheck.typecheck(
                        FeelParser.PARSER.parse(inputEntry.getTextContent()));
                return Eithers.caseOf(typedcheckResult)
                        .left(type -> {
                            if (type.equals(expectedType) || type.equals(ExpressionType.TOP)) {
                                return Collections.<ValidationResult.Builder>emptyList();
                            } else {
                                return Collections.singletonList(ValidationResult.Builder.with($ -> {
                                    $.message = "Type of input entry does not match type of input expression";
                                    $.element = rule;
                                }));
                            }
                        })
                        .right(validationResultBuilder -> Collections.singletonList(
                                validationResultBuilder.extend($ -> $.element = rule)));
            }).flatMap(List::stream).map(ValidationResult.Builder::build);
        }).collect(Collectors.toList());
    }

    @Override
    public Class<DecisionTable> getClassUnderValidation() {
        return DecisionTable.class;
    }
}
