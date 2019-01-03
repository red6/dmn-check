package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.feel.ExpressionType;
import de.redsix.dmncheck.feel.ExpressionTypeParser;
import de.redsix.dmncheck.feel.ExpressionTypes;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.util.Either;
import de.redsix.dmncheck.validators.core.SimpleValidator;
import de.redsix.dmncheck.validators.core.ValidationContext;
import org.camunda.bpm.model.dmn.instance.DmnElement;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class ElementTypeDeclarationValidator<T extends DmnElement> extends SimpleValidator<T> {

    abstract String getTypeRef(T expression);

    public boolean isApplicable(T expression, ValidationContext validationContext) {
        return true;
    }

    public List<ValidationResult> validate(T expression, ValidationContext validationContext) {
        final String expressionType = getTypeRef(expression);
        if(Objects.isNull(expressionType)) {
            return Collections.singletonList(ValidationResult.init
                    .message(getClassUnderValidation().getSimpleName() + " has no type")
                    .severity(Severity.WARNING)
                    .element(expression)
                    .build());
        } else {
            final Either<ValidationResult.Builder.ElementStep, ExpressionType> eitherType = ExpressionTypeParser.parse(expressionType, validationContext.getItemDefinitions());
            return eitherType.match(validationResult -> Collections.singletonList(validationResult.element(expression).build()), type -> {
                if (ExpressionTypes.TOP().equals(type)) {
                    return Collections.singletonList(
                            ValidationResult.init
                                    .message("TOP is an internal type and cannot be used in declarations.")
                                    .severity(Severity.ERROR)
                                    .element(expression)
                                    .build());
                } else {
                    return Collections.emptyList();
                }
            });
        }
    }
}
