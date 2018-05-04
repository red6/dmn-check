package de.redsix.dmncheck.result;

import org.camunda.bpm.model.xml.instance.ModelElementInstance;



public class ValidationResult {

    private final Severity severity;
    private final String message;
    private final ModelElementInstance element;

    private ValidationResult(final String message, final ModelElementInstance element, final Severity severity) {
        this.message = message;
        this.element = element;
        this.severity = severity;
    }

    public Severity getSeverity() {
        return severity;
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
            SeverityStep message(String message);
        }

        public interface SeverityStep extends ElementStep {
            ElementStep severity(Severity type);
            String getMessage();
        }

        public interface ElementStep {
            BuildStep element(ModelElementInstance element);
            Severity getType();
            String getMessage();
        }

        public interface BuildStep {
            ModelElementInstance getElement();
            Severity getType();
            String getMessage();
            ValidationResult build();
        }

        public static final MessageStep init = message -> (new SeverityStep() {

            Severity type = Severity.ERROR;

            @Override
            public BuildStep element(ModelElementInstance element) {
                return new BuildStep() {

                    @Override
                    public ModelElementInstance getElement() {
                        return element;
                    }

                    @Override
                    public Severity getType() {
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
            public Severity getType() {
                return type;
            }

            @Override
            public ElementStep severity(Severity type) {
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
