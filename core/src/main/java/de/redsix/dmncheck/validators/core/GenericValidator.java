package de.redsix.dmncheck.validators.core;

import de.redsix.dmncheck.result.ValidationResult;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class GenericValidator<S extends ModelElementInstance, T extends ModelElementInstance> implements Validator {

    protected abstract boolean isApplicable(final S element, ValidationContext validationContext);

    protected abstract List<ValidationResult> validate(final T element, ValidationContext validationContext);

    protected abstract Class<S> getClassUsedToCheckApplicability();

    protected abstract Class<T> getClassUnderValidation();

    public List<ValidationResult> apply(final DmnModelInstance dmnModelInstance) {
        final ValidationContext validationContext = new ValidationContext(dmnModelInstance);

        final Collection<S> elements = dmnModelInstance.getModelElementsByType(getClassUsedToCheckApplicability());
        return elements.stream()
                .filter(element -> isApplicable(element, validationContext))
                .flatMap(this::getElementsUnderValidation)
                .flatMap(element -> validate(element, validationContext).stream())
                .collect(Collectors.toList());
    }

    private Stream<T> getElementsUnderValidation(final S element) {
        final Stream<T> childElementsUnderValidation = element.getChildElementsByType(getClassUnderValidation()).stream();

        if (getClassUnderValidation().isInstance(element)) {
            return Stream.concat(childElementsUnderValidation, Stream.of((T) element));
        } else {
            return childElementsUnderValidation;
        }
    }
}
