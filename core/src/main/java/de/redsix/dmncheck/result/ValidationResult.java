package de.redsix.dmncheck.result;

import java.util.Optional;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A validation result consists of the following:
 *
 * <p>1) A message that describes the validation result 2) A severity indicating how severe the result is 3) A reference
 * to the DMN element that caused result
 *
 * <p>In this context a validation result always describes an error or a warning. There is currently no way to express
 * positive validation results, except returning no validation results at all.
 */
public class ValidationResult {

    private final Severity severity;
    private final String message;
    private final ModelElementInstance element;
    private final @Nullable String documentation;

    private ValidationResult(final String message, final ModelElementInstance element, final Severity severity) {
        this(message, element, severity, null);
    }

    private ValidationResult(
            final String message,
            final ModelElementInstance element,
            final Severity severity,
            final @Nullable String documentation) {
        this.message = message;
        this.element = element;
        this.severity = severity;
        this.documentation = documentation;
    }

    /**
     * Returns a copy of this validation result that links to the given documentation.
     *
     * @param documentation URL of a detailed description of the result and possible solutions
     * @return validation result with documentation
     */
    public ValidationResult withDocumentation(final String documentation) {
        return new ValidationResult(message, element, severity, documentation);
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

    public Optional<String> getDocumentation() {
        return Optional.ofNullable(documentation);
    }

    @Override
    public String toString() {
        return message;
    }

    public static final Builder.MessageStep init = message -> (new Builder.SeverityStep() {

        private Severity severity = Severity.ERROR;

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
