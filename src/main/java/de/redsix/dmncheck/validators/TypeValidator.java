package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.feel.FeelParser;
import de.redsix.dmncheck.feel.FeelTypecheck;
import de.redsix.dmncheck.model.ExpressionType;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.util.Either;
import de.redsix.dmncheck.util.Eithers;
import de.redsix.dmncheck.util.Util;
import de.redsix.dmncheck.validators.core.SimpleValidator;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.DmnElement;
import org.camunda.bpm.model.dmn.instance.Rule;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
public abstract class TypeValidator extends SimpleValidator<DecisionTable> {

    abstract String errorMessage();

    abstract boolean isEmptyAllowed();

    Stream<ValidationResult> typecheck(final Rule rule, final Stream<? extends DmnElement> expressions, final Stream<String> variables,
            final Stream<Optional<ExpressionType>> types) {
        return Util.zip(expressions, variables, types, (expression, variable, optionalType) -> {
            final FeelTypecheck.Context context = new FeelTypecheck.Context();

            optionalType.ifPresent(type -> context.put(variable, type));

            return typecheckExpression(rule, expression, context, optionalType);
        }).flatMap(List::stream).map(ValidationResult.Builder::build);
    }

    Stream<ValidationResult> typecheck(final Rule rule, final Stream<? extends DmnElement> expressions,
            final Stream<Optional<ExpressionType>> types) {
        return Util.zip(expressions, types, (expression, type) -> {
            final FeelTypecheck.Context emptyContext = new FeelTypecheck.Context();

            return typecheckExpression(rule, expression, emptyContext, type);
        }).flatMap(List::stream).map(ValidationResult.Builder::build);
    }

    private List<ValidationResult.Builder> typecheckExpression(Rule rule, DmnElement inputEntry, FeelTypecheck.Context context,
            Optional<ExpressionType> expectedType) {
        final Either<ExpressionType, ValidationResult.Builder> typedcheckResult = FeelParser.parse(inputEntry.getTextContent())
                .bind(feelExpression -> FeelTypecheck.typecheck(context, feelExpression));

        return Eithers.caseOf(typedcheckResult).left(type -> {
            if (expectedType.map(type::equals).orElse(true) || isEmptyAllowed() && type.equals(ExpressionType.TOP)) {
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
    public Class<DecisionTable> getClassUnderValidation() {
        return DecisionTable.class;
    }
}
