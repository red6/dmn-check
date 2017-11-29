package de.redsix.dmncheck.result;

import org.camunda.bpm.model.xml.instance.ModelElementInstance;

import java.util.function.Consumer;

public class  ValidationResult {

    private final ValidationResultType validationResultType;

    private final String message;

    private final ModelElementInstance element;

    private ValidationResult(String message, ValidationResultType validationResultType, ModelElementInstance element) {
        this.message = message;
        this.validationResultType = validationResultType;
        this.element = element;
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

    public enum Builder {
        instance;

        public ValidationResultType validationResultType = ValidationResultType.ERROR;
        public String message;
        public ModelElementInstance element;

        public Builder with(Consumer<Builder> builderConsumer) {
            builderConsumer.accept(this);
            return this;
        }

        public ValidationResult build() {
            return new ValidationResult(message, validationResultType, element);
        }
    }
}
