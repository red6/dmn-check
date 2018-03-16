package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.model.ExpressionType;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.result.ValidationResultType;
import de.redsix.dmncheck.validators.core.Validator;
import org.camunda.bpm.model.dmn.instance.DmnElement;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@ParametersAreNonnullByDefault
public interface ElementTypeDeclarationValidator<T extends DmnElement> extends Validator<T> {

    String getTypeRef(T expression);

    default boolean isApplicable(T expression) {
        return true;
    }

    default List<ValidationResult> validate(T expression) {
        final String expressionType = getTypeRef(expression);
        if(Objects.isNull(expressionType)) {
            return Collections.singletonList(ValidationResult.Builder.with($ -> {
                $.message = getClassUnderValidation().getSimpleName() + " has no type";
                $.element = expression;
                $.type = ValidationResultType.WARNING;
            }).build());
        } else
        if (ExpressionType.isNotValid(expressionType)) {
            return Collections.singletonList(ValidationResult.Builder.with($ -> {
                $.message = getClassUnderValidation().getSimpleName() + " uses an unsupported type";
                $.element = expression;
            }).build());
        } else {
            return Collections.emptyList();
        }
    }
}
