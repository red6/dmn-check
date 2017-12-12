package de.redsix.dmncheck.result;

import org.camunda.bpm.model.dmn.instance.InputEntry;
import org.camunda.bpm.model.dmn.instance.OutputEntry;
import org.camunda.bpm.model.dmn.instance.Rule;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class PrettyPrintValidationResults {

    public static String prettify(File file, Map<ModelElementInstance, Map<ValidationResultType, List<ValidationResult>>> validationResults) {
        final StringBuilder sb = new StringBuilder();

        for (Map.Entry<ModelElementInstance, Map<ValidationResultType, List<ValidationResult>>> validationResult : validationResults
                .entrySet()) {
            sb.append("Element '").append(delegate(validationResult.getKey())).append("'");
            sb.append(" of type '").append(validationResult.getKey().getElementType().getTypeName()).append("'");
            sb.append(" in file ").append(file.getName());
            sb.append(" has the following validation results ");
            sb.append(validationResult.getValue());
        }

        return sb.toString();
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
