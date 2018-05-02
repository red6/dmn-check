package de.redsix.dmncheck.result;

import org.apache.maven.plugin.logging.Log;
import org.camunda.bpm.model.dmn.instance.InputEntry;
import org.camunda.bpm.model.dmn.instance.OutputEntry;
import org.camunda.bpm.model.dmn.instance.Rule;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class PrettyPrintValidationResults {

    private PrettyPrintValidationResults() {

    }

    public static void logPrettified(final File file, final List<ValidationResult> validationResults, final Log log) {
        validationResults.sort(Comparator.comparing(ValidationResult::getValidationResultType).reversed());

        for (ValidationResult validationResult : validationResults) {
            final String errorMessage =
                    "Element '" + delegate(validationResult.getElement()) + "'" + " of type '" + validationResult.getElement()
                            .getElementType().getTypeName() + "'" + " in file " + file.getName() + " has the following validation results "
                            + validationResult.getMessage();
            getLoggingMethod(validationResult.getValidationResultType(), log).accept(errorMessage);
        }
    }

    private static String delegate(final ModelElementInstance element) {
        if (element instanceof Rule) {
            return prettify((Rule) element);
        } else {
            return element.getRawTextContent();
        }
    }

    private static String prettify(final Rule rule) {
        return Stream.concat(rule.getInputEntries().stream().map(InputEntry::getTextContent),
                rule.getOutputEntries().stream().map(OutputEntry::getTextContent)).collect(Collectors.joining(","));
    }

    private static Consumer<CharSequence> getLoggingMethod(final ValidationResultType validationResultType, final Log log) {
        switch (validationResultType) {
            case ERROR: return log::error;
            case WARNING: return log::warn;
            default: return log::error;
        }
    }
}
