package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.feel.ExpressionType;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.result.ValidationResultType;
import de.redsix.dmncheck.validators.core.SimpleValidator;
import org.camunda.bpm.model.dmn.instance.DmnElement;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@ParametersAreNonnullByDefault
public abstract class ElementTypeDeclarationValidator<T extends DmnElement> extends SimpleValidator<T> {

    abstract String getTypeRef(T expression);

    public boolean isApplicable(T expression) {
        return true;
    }

    public List<ValidationResult> validate(T expression) {
        final String expressionType = getTypeRef(expression);
        if(Objects.isNull(expressionType)) {
            return Collections.singletonList(ValidationResult.Builder.init
                    .messageAndType(getClassUnderValidation().getSimpleName() + " has no type", ValidationResultType.WARNING)
                    .element(expression)
                    .build());
        } else
        if (ExpressionType.isNotValid(expressionType)) {
            return Collections.singletonList(ValidationResult.Builder.init
                    .message(getClassUnderValidation().getSimpleName() + " uses an unsupported type")
                    .element(expression)
                    .build());
        } else {
            return Collections.emptyList();
        }
    }
}
