package de.redsix.dmncheck.result;

import org.camunda.bpm.model.xml.instance.ModelElementInstance;


import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ValidationResult {

    private ValidationResultType validationResultType;
    private String message;
    private ModelElementInstance element;

    private ValidationResult(final String message, final ModelElementInstance element, final ValidationResultType validationResultType) {
        this.message = message;
        this.element = element;
        this.validationResultType = validationResultType;
    }

    public ValidationResultType getValidationResultType() {
        return validationResultType;
    }

    public String getMessage() {
        return message;
    }

    public ModelElementInstance getElement() {
        return element;
    }

    @Override
    public String toString() {
        return message;
    }

    public static final class Builder {

        @FunctionalInterface
        public interface MessageStep {
            default ElementStep message(String message) {
                return messageAndType(message, ValidationResultType.ERROR);
            }
            ElementStep messageAndType(String message, ValidationResultType type);
        }

        @FunctionalInterface
        public interface ElementStep {
            BuildStep element(ModelElementInstance element);
        }

        @FunctionalInterface
        public interface BuildStep {
            ValidationResult build();
        }

        public static final MessageStep init = (message, type) -> element -> () -> new ValidationResult(message, element, type);
    }
}
