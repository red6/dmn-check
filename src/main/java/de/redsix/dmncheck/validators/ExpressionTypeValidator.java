package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.model.ExpressionTypeEnum;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.core.Validator;
import org.camunda.bpm.model.dmn.instance.Expression;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public interface ExpressionTypeValidator<T extends Expression> extends Validator<T> {

    default boolean isApplicable(T expression) {
        return true;
    }

    default List<ValidationResult> validate(T expression) {
        final String expressionType = expression.getTypeRef();
        if(Objects.isNull(expressionType)) {
            return Collections.singletonList(ValidationResult.Builder.instance.with($ -> {
                $.message = getClassUnderValidation() + " has no type";
                $.element = expression;
            }).build());
        } else
        if (ExpressionTypeEnum.isValid(expressionType)) {
            return Collections.singletonList(ValidationResult.Builder.instance.with($ -> {
                $.message = getClassUnderValidation() + " uses an unsupported type";
                $.element = expression;
            }).build());
        } else {
            return Collections.emptyList();
        }
    }
}
