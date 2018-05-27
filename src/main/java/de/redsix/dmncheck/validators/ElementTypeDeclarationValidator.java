package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.feel.ExpressionType;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.validators.core.SimpleValidator;
import org.camunda.bpm.model.dmn.instance.DmnElement;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class ElementTypeDeclarationValidator<T extends DmnElement> extends SimpleValidator<T> {

    abstract String getTypeRef(T expression);

    public boolean isApplicable(T expression) {
        return true;
    }

    public List<ValidationResult> validate(T expression) {
        final String expressionType = getTypeRef(expression);
        if(Objects.isNull(expressionType)) {
            return Collections.singletonList(ValidationResult.init
                    .message(getClassUnderValidation().getSimpleName() + " has no type")
                    .severity(Severity.WARNING)
                    .element(expression)
                    .build());
        } else
        if (ExpressionType.isNotValid(expressionType)) {
            return Collections.singletonList(ValidationResult.init
                    .message(getClassUnderValidation().getSimpleName() + " uses an unsupported type")
                    .element(expression)
                    .build());
        } else {
            return Collections.emptyList();
        }
    }
}
