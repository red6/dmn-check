package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.core.SimpleValidator;
import de.redsix.dmncheck.validators.core.ValidationContext;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.Input;
import org.camunda.bpm.model.dmn.instance.Output;

public class DuplicateColumnLabelValidator extends SimpleValidator<DecisionTable> {

    @Override
    public boolean isApplicable(final DecisionTable decisionTable, final ValidationContext validationContext) {
        return true;
    }

    @Override
    public List<ValidationResult> validate(final DecisionTable decisionTable, final ValidationContext validationContext) {
        return Stream.concat(
                        validateColumn(decisionTable, decisionTable.getInputs(), Input::getLabel).stream(),
                        validateColumn(decisionTable, decisionTable.getOutputs(), Output::getLabel).stream())
                .toList();
    }

    private <T> List<ValidationResult> validateColumn(
            final DecisionTable decisionTable, final Collection<T> columns, final Function<T, String> getLabel) {
        final List<String> labels = columns.stream().map(getLabel).toList();

        return labels.stream()
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
            .entrySet().stream()
            .filter(e -> e.getValue() > 1)
            .map(Map.Entry::getKey)
             .map(label -> ValidationResult.init
                     .message("Column with label '" + label + "' is used more than once")
                     .severity(Severity.WARNING)
                     .element(decisionTable)
                     .build())
             .toList();
    }

    @Override
    public Class<DecisionTable> getClassUnderValidation() {
        return DecisionTable.class;
    }
}
