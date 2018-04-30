package de.redsix.dmncheck.result;

import org.camunda.bpm.model.xml.instance.ModelElementInstance;



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
            TypeStep message(String message);
        }

        public interface TypeStep extends ElementStep {
            ElementStep type(ValidationResultType type);
            String getMessage();
        }

        public interface ElementStep {
            BuildStep element(ModelElementInstance element);
            ValidationResultType getType();
            String getMessage();
        }

        public interface BuildStep {
            ModelElementInstance getElement();
            ValidationResultType getType();
            String getMessage();
            ValidationResult build();
        }

        public static final MessageStep init = message -> (new TypeStep() {

            ValidationResultType type = ValidationResultType.ERROR;

            @Override
            public BuildStep element(ModelElementInstance element) {
                return new BuildStep() {

                    @Override
                    public ModelElementInstance getElement() {
                        return element;
                    }

                    @Override
                    public ValidationResultType getType() {
                        return type;
                    }

                    @Override
                    public String getMessage() {
                        return message;
                    }

                    @Override
                    public ValidationResult build() {
                        return new ValidationResult(message, element, type);
                    }
                };
            }

            @Override
            public ValidationResultType getType() {
                return type;
            }

            @Override
            public ElementStep type(ValidationResultType type) {
                this.type = type;
                return this;
            }

            @Override
            public String getMessage() {
                return message;
            }
        });
    }
}
