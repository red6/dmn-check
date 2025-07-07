package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.core.SimpleValidator;
import de.redsix.dmncheck.validators.core.ValidationContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.camunda.bpm.model.dmn.instance.DmnElement;
import org.camunda.bpm.model.dmn.instance.NamedElement;

public abstract class IdAndNameValidator<T extends DmnElement & NamedElement>
    extends SimpleValidator<T> {

    protected abstract String getName();

    @Override
    public boolean isApplicable(
        T element,
        ValidationContext validationContext
    ) {
        return true;
    }

    @Override
    public List<ValidationResult> validate(
        T element,
        ValidationContext validationContext
    ) {
        final List<ValidationResult> validationResults = new ArrayList<>();

        if (Objects.isNull(element.getId())) {
            validationResults.add(
                ValidationResult.init
                    .message("A " + this.getName() + " has no id.")
                    .element(element)
                    .build()
            );
        }

        if (Objects.isNull(element.getName())) {
            validationResults.add(
                ValidationResult.init
                    .message("A " + this.getName() + " has no name.")
                    .severity(Severity.WARNING)
                    .element(element)
                    .build()
            );
        }

        return validationResults;
    }
}
