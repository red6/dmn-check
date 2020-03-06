package de.redsix.dmncheck.plugin;

import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import org.camunda.bpm.model.dmn.instance.InputEntry;
import org.camunda.bpm.model.dmn.instance.OutputEntry;
import org.camunda.bpm.model.dmn.instance.Rule;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.checkerframework.checker.nullness.qual.PolyNull;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class PrettyPrintValidationResults {

    private PrettyPrintValidationResults() {

    }

    public static class CustomLogger {
        protected Consumer<@PolyNull CharSequence> info;
        protected Consumer<@PolyNull CharSequence> warn;
        protected Consumer<@PolyNull CharSequence> error;

        public CustomLogger(final Consumer<@PolyNull CharSequence> info, final Consumer<@PolyNull CharSequence> warn, final Consumer<@PolyNull CharSequence> error) {
            this.info = info;
            this.warn = warn;
            this.error = error;
        }
    }
    public static void logPrettified(final File file, final List<ValidationResult> validationResults, final CustomLogger log) {
        log.info.accept("Validation results for file " + file.getAbsolutePath());

        validationResults.sort(Comparator.comparing(ValidationResult::getSeverity).reversed());

        for (ValidationResult validationResult : validationResults) {
            final String errorMessage =
                    "Element '" + delegate(validationResult.getElement()) + "'" + " of type '" + validationResult.getElement()
                                                                                                                 .getElementType().getTypeName() + "'" + " has the following validation result: " + validationResult
                            .getMessage();
            getLoggingMethod(validationResult.getSeverity(), log).accept(errorMessage);
        }
    }

    private static String delegate(final ModelElementInstance element) {
        if (element instanceof Rule) {
            return prettify((Rule) element);
        } else {
            return element.getRawTextContent().trim();
        }
    }

    private static String prettify(final Rule rule) {
        return Stream.concat(rule.getInputEntries().stream().map(InputEntry::getTextContent),
                             rule.getOutputEntries().stream().map(OutputEntry::getTextContent)).collect(Collectors.joining(","));
    }

    private static Consumer<@PolyNull CharSequence> getLoggingMethod(final Severity severity, final CustomLogger logger) {
        if (severity == Severity.WARNING) {
            return logger.warn;
        }

        return logger.error;
    }
}
