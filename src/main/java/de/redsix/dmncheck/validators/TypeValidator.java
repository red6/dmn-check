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
import org.camunda.bpm.model.dmn.instance.DmnElement;
import org.camunda.bpm.model.dmn.instance.Rule;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public interface TypeValidator extends Validator<DecisionTable> {

    String errorMessage();

    boolean isEmptyAllowed();

    default Stream<ValidationResult> typecheck(final Rule rule, final Stream<? extends DmnElement> expressions,
            final Stream<String> variables, final Stream<ExpressionType> types) {
        return Util.zip(expressions, variables, types, (expression, variable, type) -> {
            final FeelTypecheck.Context context = new FeelTypecheck.Context();
            context.put(variable, type);

            return typecheckExpression(rule, expression, context, type);
        }).flatMap(List::stream).map(ValidationResult.Builder::build);
    }

    default Stream<ValidationResult> typecheck(final Rule rule, final Stream<? extends DmnElement> expressions,
            final Stream<ExpressionType> types) {
        return Util.zip(expressions, types, (expression, type) -> {
            final FeelTypecheck.Context emptyContext = new FeelTypecheck.Context();

            return typecheckExpression(rule, expression, emptyContext, type);
        }).flatMap(List::stream).map(ValidationResult.Builder::build);
    }

    default List<ValidationResult.Builder> typecheckExpression(Rule rule, DmnElement inputEntry, FeelTypecheck.Context context,
            ExpressionType expectedType) {
        final Either<ExpressionType, ValidationResult.Builder> typedcheckResult = FeelParser.parse(inputEntry.getTextContent())
                .bind(feelExpression -> FeelTypecheck.typecheck(context, feelExpression));

        return Eithers.caseOf(typedcheckResult).left(type -> {
            if (type.equals(expectedType) || isEmptyAllowed() && type.equals(ExpressionType.TOP)) {
                return Collections.<ValidationResult.Builder>emptyList();
            } else {
                return Collections.singletonList(ValidationResult.Builder.with($ -> {
                    $.message = errorMessage();
                    $.element = rule;
                }));
            }
        }).right(validationResultBuilder -> Collections.singletonList(validationResultBuilder.extend($ -> $.element = rule)));
    }

    @Override
    default Class<DecisionTable> getClassUnderValidation() {
        return DecisionTable.class;
    }
}
