package de.redsix.dmncheck.result;

import org.camunda.bpm.model.xml.instance.ModelElementInstance;

public class  ValidationResult {

    private ValidationResultType validationResultType = ValidationResultType.ERROR;
    private String message;
    private ModelElementInstance element;

    private ValidationResult() {

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

    public interface Message {
        Element message(String message);
    }

    public interface Element {
        Element type(ValidationResultType type);
        Build element(ModelElementInstance element);
        String getMessage();
    }

    public interface Build {
        ModelElementInstance getElement();
        ValidationResultType getType();
        ValidationResult build();
    }

    public final static class Builder implements Message, Element, Build {

        private ValidationResult validationResult;

        private Builder() {
            validationResult = new ValidationResult();
        }

        public static ValidationResult.Message validationResult() {
            return new Builder();
        }

        @Override
        public Element message(String message) {
            this.validationResult.message = message;
            return this;
        }

        @Override
        public String getMessage() {
            return this.validationResult.message;
        }

        @Override
        public Element type(ValidationResultType type) {
            this.validationResult.validationResultType = type;
            return this;
        }

        @Override
        public Build element(ModelElementInstance element) {
            this.validationResult.element = element;
            return this;
        }

        @Override
        public ModelElementInstance getElement() {
            return this.validationResult.element;
        }

        @Override
        public ValidationResultType getType() {
            return this.validationResult.validationResultType;
        }

        @Override
        public ValidationResult build() {
            final ValidationResult builtValidationResult = validationResult;
            validationResult = new ValidationResult();
            return builtValidationResult;
        }
    }
}
