package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.feel.ExpressionType;
import de.redsix.dmncheck.feel.ExpressionTypes;
import de.redsix.dmncheck.feel.FeelParser;
import de.redsix.dmncheck.feel.FeelTypecheck;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.util.Either;
import de.redsix.dmncheck.util.ProjectClassLoader;
import de.redsix.dmncheck.util.Util;
import de.redsix.dmncheck.validators.core.SimpleValidator;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.DmnElement;
import org.camunda.bpm.model.dmn.instance.Rule;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.redsix.dmncheck.util.Eithers.left;
import static de.redsix.dmncheck.util.Eithers.right;

public abstract class TypeValidator extends SimpleValidator<DecisionTable> {

    abstract String errorMessage();

    Stream<ValidationResult> typecheck(final Rule rule, final Stream<? extends DmnElement> expressions, final Stream<String> variables,
            final Stream<ExpressionType> types) {
        return Util.zip(expressions, variables, types, (expression, variable, type) -> {
            final FeelTypecheck.Context context = new FeelTypecheck.Context();

            context.put(variable, type);

            return typecheckExpression(rule, expression, context, type);
        }).filter(Optional::isPresent).map(Optional::get).map(ValidationResult.Builder.BuildStep::build);
    }

    Stream<ValidationResult> typecheck(final Rule rule, final Stream<? extends DmnElement> expressions,
            final Stream<ExpressionType> types) {
        return Util.zip(expressions, types, (expression, type) -> {
            final FeelTypecheck.Context emptyContext = new FeelTypecheck.Context();

            return typecheckExpression(rule, expression, emptyContext, type);
        }).filter(Optional::isPresent).map(Optional::get).map(ValidationResult.Builder.BuildStep::build);
    }

    private Optional<ValidationResult.Builder.BuildStep> typecheckExpression(Rule rule, DmnElement inputEntry, FeelTypecheck.Context context,
            ExpressionType expectedType) {
        return FeelParser.parse(inputEntry.getTextContent()).bind(feelExpression -> FeelTypecheck.typecheck(context, feelExpression))
                .map(type -> {
                    if (type.isSubtypeOf(ExpressionTypes.STRING()) && ExpressionTypes.getClassName(expectedType).isPresent()) {
                        return checkEnumValue(ExpressionTypes.getClassName(expectedType).get(), inputEntry.getTextContent(), rule);
                    } else if (type.isSubtypeOf(expectedType) || ExpressionTypes.TOP().equals(type)) {
                        return Optional.<ValidationResult.Builder.BuildStep>empty();
                    } else {
                        return Optional.of(ValidationResult.init.message(errorMessage()).element(rule));
                    }
                }).match(Function.identity(), validationResultBuilder -> Optional.of(validationResultBuilder.element(rule)));
    }

    private Optional<ValidationResult.Builder.BuildStep> checkEnumValue(final String className, final String stringValue, final Rule rule) {
        return loadEnum(className)
                .bind(this::isEnum)
                .bind(clazz -> doesStringBelongToEnum(className, stringValue, clazz))
                .match((__) -> Optional.empty(), validationResultBuilder -> Optional.of(validationResultBuilder.element(rule)));
    }

    private Either<? extends Class<?>, ValidationResult.Builder.ElementStep> doesStringBelongToEnum(String className, String stringValue,
            Class<?> clazz) {
        final List<String> enumConstants = Arrays.stream(((Class<? extends Enum>) clazz).getEnumConstants()).map(Enum::name)
                .collect(Collectors.toList());
        final String value = stringValue.substring(1, stringValue.length() - 1);

        if (enumConstants.contains(value)) {
            return left(clazz);
        } else {
            return right(ValidationResult.init.message("Value " + stringValue + " does not belong to " + className));
        }
    }

    private Either<Class<?>, ValidationResult.Builder.ElementStep> loadEnum(final String className) {
        try {
            return left(ProjectClassLoader.instance.classLoader.loadClass(className));
        } catch (ClassNotFoundException e) {
            return right(ValidationResult.init.message("Class " + className + " not found on project classpath."));
        }
    }

    private Either<Class<?>, ValidationResult.Builder.ElementStep> isEnum(Class<?> clazz) {
        if (clazz.isEnum()) {
            return left(clazz);
        } else {
            return right(ValidationResult.init.message("Class " + clazz.getCanonicalName() + " is no enum."));
        }
    }

    @Override
    public Class<DecisionTable> getClassUnderValidation() {
        return DecisionTable.class;
    }
}
