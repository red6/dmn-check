package de.redsix.dmncheck.result;

import org.camunda.bpm.model.xml.instance.ModelElementInstance;

import java.io.File;
import java.util.List;
import java.util.Map;

public final class PrettyPrintValidationResults {

    public static String prettify(File file, Map<ModelElementInstance, Map<ValidationResultType, List<ValidationResult>>> validationResults) {
        final StringBuilder sb = new StringBuilder();

        for (Map.Entry<ModelElementInstance, Map<ValidationResultType, List<ValidationResult>>> validationResult : validationResults
                .entrySet()) {
            sb.append("Element '").append(validationResult.getKey().getRawTextContent()).append("'");
            sb.append(" of type '").append(validationResult.getKey().getElementType().getTypeName()).append("'");
            sb.append(" in file ").append(file.getName());
            sb.append(" has the following validation results ");
            sb.append(validationResult.getValue());
        }

        return sb.toString();
    }
}
