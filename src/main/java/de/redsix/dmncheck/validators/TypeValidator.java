package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.feel.ExpressionTypes;
import de.redsix.dmncheck.feel.FeelParser;
import de.redsix.dmncheck.feel.FeelTypecheck;
import de.redsix.dmncheck.feel.ExpressionType;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.util.Util;
import de.redsix.dmncheck.validators.core.SimpleValidator;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.DmnElement;
import org.camunda.bpm.model.dmn.instance.Rule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class TypeValidator extends SimpleValidator<DecisionTable> {

    abstract String errorMessage();

    abstract boolean isEmptyAllowed();

    Stream<ValidationResult> typecheck(final Rule rule, final Stream<? extends DmnElement> expressions, final Stream<String> variables,
            final Stream<ExpressionType> types) {
        return Util.zip(expressions, variables, types, (expression, variable, type) -> {
            final FeelTypecheck.Context context = new FeelTypecheck.Context();

            context.put(variable, type);

            return typecheckExpression(rule, expression, context, type);
        }).flatMap(List::stream).map(ValidationResult.Builder.BuildStep::build);
    }

    Stream<ValidationResult> typecheck(final Rule rule, final Stream<? extends DmnElement> expressions,
            final Stream<ExpressionType> types) {
        return Util.zip(expressions, types, (expression, type) -> {
            final FeelTypecheck.Context emptyContext = new FeelTypecheck.Context();

            return typecheckExpression(rule, expression, emptyContext, type);
        }).flatMap(List::stream).map(ValidationResult.Builder.BuildStep::build);
    }

    private List<ValidationResult.Builder.BuildStep> typecheckExpression(Rule rule, DmnElement inputEntry, FeelTypecheck.Context context,
            ExpressionType expectedType) {
        return FeelParser.parse(inputEntry.getTextContent()).bind(feelExpression -> FeelTypecheck.typecheck(context, feelExpression))
                .map(type -> {
                    if (type.isSubtypeOf(ExpressionTypes.STRING()) && ExpressionTypes.getClassName(expectedType).isPresent()) {
                        return checkEnumValue(ExpressionTypes.getClassName(expectedType).get(), inputEntry.getTextContent(), rule);
                    } else if (type.isSubtypeOf(expectedType) || isEmptyAllowed() && ExpressionTypes.TOP().equals(type)) {
                        return Collections.<ValidationResult.Builder.BuildStep>emptyList();
                    } else {
                        return Collections.singletonList(ValidationResult.init.message(errorMessage()).element(rule));
                    }
                }).match(Function.identity(), validationResultBuilder -> Collections.singletonList(validationResultBuilder.element(rule)));
    }

    private List<ValidationResult.Builder.BuildStep> checkEnumValue(final String className, final String stringValue, final Rule rule) {
        final List<Class<? extends Enum>> enums = new ArrayList<>();
        new FastClasspathScanner(className).matchSubclassesOf(Enum.class, enums::add).scan();

        if (enums.size() == 1) {
            final List<String> foo = Arrays.stream(enums.get(0).getEnumConstants()).map(Enum::name).collect(Collectors.toList());
            final String value = stringValue.substring(1, stringValue.length() -1 );
            if (foo.contains(value)) {
                return Collections.emptyList();
            } else {
                return Collections.singletonList(ValidationResult.init.message("Value " + stringValue + " does not belong to " + className).element(rule));
            }
        } else {
            return Collections.singletonList(ValidationResult.init.message("Found " + className + " more than once on the classpath.").element(rule));
        }
    }

    @Override
    public Class<DecisionTable> getClassUnderValidation() {
        return DecisionTable.class;
    }
}
