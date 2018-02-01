package de.redsix.dmncheck.result;

import org.camunda.bpm.model.dmn.instance.InputEntry;
import org.camunda.bpm.model.dmn.instance.OutputEntry;
import org.camunda.bpm.model.dmn.instance.Rule;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class PrettyPrintValidationResults {

    public static List<String> prettify(File file, Map<ModelElementInstance, Map<ValidationResultType, List<ValidationResult>>> validationResults) {
        final List<String> errorMessages = new ArrayList<>();

        for (Map.Entry<ModelElementInstance, Map<ValidationResultType, List<ValidationResult>>> validationResult : validationResults
                .entrySet()) {
            String errorMessage = "Element '" + delegate(validationResult.getKey()) + "'" + " of type '" + validationResult.getKey().getElementType()
                    .getTypeName() + "'" + " in file " + file.getName() + " has the following validation results " + validationResult
                    .getValue();

            errorMessages.add(errorMessage);
        }

        return errorMessages;
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
}
