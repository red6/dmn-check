package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.result.ValidationResultType;
import de.redsix.dmncheck.validators.core.SimpleValidator;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.Input;
import org.camunda.bpm.model.dmn.instance.Output;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
public class DuplicateColumnLabelValidator extends SimpleValidator<DecisionTable> {

    @Override
    public boolean isApplicable(DecisionTable decisionTable) {
        return true;
    }

    @Override
    public List<ValidationResult> validate(DecisionTable decisionTable) {
        return Stream.concat(validateColumn(decisionTable, decisionTable.getInputs(), Input::getLabel).stream(),
                validateColumn(decisionTable, decisionTable.getOutputs(), Output::getLabel).stream())
                .collect(Collectors.toList());
    }

    private <T> List<ValidationResult> validateColumn(DecisionTable decisionTable, Collection<T> columns, Function<T, String> getLabel) {
        final List<String> labels = columns.stream().map(getLabel).collect(Collectors.toList());

        return labels.stream().filter(
                label -> Collections.frequency(labels, label) > 1).distinct().map(
                label -> ValidationResult.Builder.init
                        .message("Column with label '" + label + "' is used more than once")
                        .type(ValidationResultType.WARNING)
                        .element(decisionTable)
                        .build()).collect(Collectors.toList());

    }

    @Override
    public Class<DecisionTable> getClassUnderValidation() {
        return DecisionTable.class;
    }
}
