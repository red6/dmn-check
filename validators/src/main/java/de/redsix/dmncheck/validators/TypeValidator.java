package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.feel.ExpressionType;
import de.redsix.dmncheck.feel.ExpressionTypes;
import de.redsix.dmncheck.feel.FeelParser;
import de.redsix.dmncheck.feel.FeelTypecheck;
import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.util.*;
import de.redsix.dmncheck.validators.core.SimpleValidator;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.instance.DmnElement;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.redsix.dmncheck.util.Eithers.left;
import static de.redsix.dmncheck.util.Eithers.right;

public abstract class TypeValidator<T extends ModelElementInstance> extends SimpleValidator<T> {

    TopLevelExpressionLanguage toplevelExpressionLanguage = new TopLevelExpressionLanguage(null);

    abstract String errorMessage();

    @Override
    public List<ValidationResult> apply(final DmnModelInstance dmnModelInstance) {
        toplevelExpressionLanguage = new TopLevelExpressionLanguage(dmnModelInstance.getDefinitions().getExpressionLanguage());
        return super.apply(dmnModelInstance);
    }

    Stream<ValidationResult> typecheck(final DmnElement dmnElement, final Stream<Expression> expressions, final Stream<String> variables,
                                       final Stream<ExpressionType> types) {
        final Stream<Optional<ValidationResult.Builder.ElementStep>> intermediateResults = Util
                .zip(expressions, variables, types, (expression, variable, type) -> {
                    final FeelTypecheck.Context context = new FeelTypecheck.Context();

                    context.put(variable, type);

                    return typecheckExpression(expression, context, type);
                });

        return buildValidationResults(intermediateResults, dmnElement);
    }

    Stream<ValidationResult> typecheck(final DmnElement dmnElement, final Stream<Expression> expressionStream,
            final Stream<ExpressionType> types) {
        final Stream<Optional<ValidationResult.Builder.ElementStep>> intermediateResults = Util
                .zip(expressionStream, types, (unaryTest, type) -> {
                    final FeelTypecheck.Context emptyContext = new FeelTypecheck.Context();

                    return typecheckExpression(unaryTest, emptyContext, type);
                });

        return buildValidationResults(intermediateResults, dmnElement);
    }

    private Optional<ValidationResult.Builder.ElementStep> typecheckExpression(Expression expression, FeelTypecheck.Context context,
                                                                               ExpressionType expectedType) {
        return FeelParser.parse(expression).bind(feelExpression -> FeelTypecheck.typecheck(context, feelExpression))
                .map(type -> {
                    if (type.isSubtypeOf(ExpressionTypes.STRING()) && ExpressionTypes.getClassName(expectedType).isPresent()) {
                        return checkEnumValue(ExpressionTypes.getClassName(expectedType).get(), expression.textContent);
                    } else if (type.isSubtypeOf(expectedType) || ExpressionTypes.TOP().equals(type)) {
                        return Optional.<ValidationResult.Builder.ElementStep>empty();
                    } else {
                        return Optional.of(ValidationResult.init.message(errorMessage()).severity(Severity.ERROR));
                    }
                }).match(Optional::of, Function.identity());
    }

    private Optional<ValidationResult.Builder.ElementStep> checkEnumValue(final String className, final String stringValue) {
        return loadEnum(className)
                .bind(this::isEnum)
                .bind(clazz -> doesStringBelongToEnum(className, stringValue, clazz))
                .match( Optional::of, (__) -> Optional.empty());
    }

    private Either<ValidationResult.Builder.ElementStep, Class<?>> doesStringBelongToEnum(String className, String stringValue,
            Class<? extends Enum<?>> clazz) {
        final Enum<?>[] enumConstants = clazz.getEnumConstants();
        final List<String> enumConstantNames = Arrays.stream(enumConstants == null ? new Enum<?>[] {} : enumConstants).map(Enum::name)
                .collect(Collectors.toList());
        final String value = stringValue.substring(1, stringValue.length() - 1);

        if (enumConstantNames.contains(value)) {
            return right(clazz);
        } else {
            return left(ValidationResult.init.message("Value " + stringValue + " does not belong to " + className));
        }
    }

    private Either<ValidationResult.Builder.ElementStep, Class<?>> loadEnum(final String className) {
        try {
            if (ProjectClassLoader.INSTANCE.classLoader != null) {
                return right(ProjectClassLoader.INSTANCE.classLoader.loadClass(className));
            } else {
                return left(ValidationResult.init.message("Classloader of project under validation not found"));
            }
        } catch (ClassNotFoundException e) {
            return left(ValidationResult.init.message("Class " + className + " not found on project classpath."));
        }
    }

    private Either<ValidationResult.Builder.ElementStep, Class<? extends Enum<?>>> isEnum(Class<?> clazz) {
        if (clazz.isEnum()) {
            // checkerframework cannot figure out the types when using as clazz.asSubclass(Enum.class) because asSubclass introduces
            // a fresh type variable. Therefore we cast the value to the correct type.
            // equality constraints: Class<? extends Enum<?>>
            // lower bounds: Class<CAP#1>
            return right((Class<? extends Enum<?>>)clazz);
        } else {
            return left(ValidationResult.init.message("Class " + clazz.getCanonicalName() + " is no enum."));
        }
    }

    private Stream<ValidationResult> buildValidationResults(final Stream<Optional<ValidationResult.Builder.ElementStep>> elementSteps,
            final DmnElement dmnElement) {
        return elementSteps
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(validationResultBuilder -> validationResultBuilder.element(dmnElement))
                .map(ValidationResult.Builder.BuildStep::build);
    }
}
