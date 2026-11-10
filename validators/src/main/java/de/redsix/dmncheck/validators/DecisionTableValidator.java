package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.core.SimpleValidator;
import de.redsix.dmncheck.validators.core.ValidationContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.operaton.bpm.model.dmn.instance.DecisionTable;
import org.operaton.bpm.model.dmn.instance.Output;
import org.operaton.bpm.model.dmn.instance.Rule;

/** Checks the structure of a decision table as defined in chapter 8 of the DMN specification. */
public class DecisionTableValidator extends SimpleValidator<DecisionTable> {

    @Override
    public Optional<String> documentation() {
        return Optional.of("https://github.com/red6/dmn-check#structure-of-decision-tables");
    }

    @Override
    public boolean isApplicable(final DecisionTable decisionTable, final ValidationContext validationContext) {
        return true;
    }

    @Override
    public List<ValidationResult> validate(final DecisionTable decisionTable, final ValidationContext validationContext) {
        final List<ValidationResult> validationResults = new ArrayList<>();
        final int inputCount = decisionTable.getInputs().size();
        final int outputCount = decisionTable.getOutputs().size();

        if (outputCount == 0) {
            validationResults.add(ValidationResult.init
                    .message("Decision table has no outputs")
                    .element(decisionTable)
                    .build());
        }

        if (outputCount > 1) {
            for (final Output output : decisionTable.getOutputs()) {
                if (output.getName() == null || output.getName().isBlank()) {
                    validationResults.add(ValidationResult.init
                            .message("Output has no name, but decision tables with more than one output require named outputs")
                            .element(output)
                            .build());
                }
            }
        }

        if (decisionTable.getRules().isEmpty()) {
            validationResults.add(ValidationResult.init
                    .message("Decision table has no rules")
                    .severity(Severity.WARNING)
                    .element(decisionTable)
                    .build());
        }

        for (final Rule rule : decisionTable.getRules()) {
            final int inputEntryCount = rule.getInputEntries().size();
            if (inputEntryCount != inputCount) {
                validationResults.add(ValidationResult.init
                        .message("Rule has " + inputEntryCount + " input entries, but the decision table has "
                                + inputCount + " inputs")
                        .element(rule)
                        .build());
            }

            final int outputEntryCount = rule.getOutputEntries().size();
            if (outputEntryCount != outputCount) {
                validationResults.add(ValidationResult.init
                        .message("Rule has " + outputEntryCount + " output entries, but the decision table has "
                                + outputCount + " outputs")
                        .element(rule)
                        .build());
            }
        }

        return validationResults;
    }

    @Override
    public Class<DecisionTable> getClassUnderValidation() {
        return DecisionTable.class;
    }
}
