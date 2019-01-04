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

    public static final Builder.MessageStep init = message -> (new Builder.SeverityStep() {

        Severity severity = Severity.ERROR;

        @Override
        public Builder.BuildStep element(ModelElementInstance element) {
            return new Builder.BuildStep() {

                @Override
                public ModelElementInstance getElement() {
                    return element;
                }

                @Override
                public Severity getSeverity() {
                    return severity;
                }

                @Override
                public String getMessage() {
                    return message;
                }

                @Override
                public ValidationResult build() {
                    return new ValidationResult(message, element, severity);
                }
            };
        }

        @Override
        public Severity getSeverity() {
            return severity;
        }

        @Override
        public Builder.ElementStep severity(Severity severity) {
            this.severity = severity;
            return this;
        }

        @Override
        public String getMessage() {
            return message;
        }
    });

    public static final class Builder {

        @FunctionalInterface
        public interface MessageStep {
            SeverityStep message(String message);
        }

        public interface SeverityStep extends ElementStep {
            ElementStep severity(Severity severity);
            String getMessage();
        }

        public interface ElementStep {
            BuildStep element(ModelElementInstance element);
            Severity getSeverity();
            String getMessage();
        }

        public interface BuildStep {
            ModelElementInstance getElement();
            Severity getSeverity();
            String getMessage();
            ValidationResult build();
        }

    }
}
