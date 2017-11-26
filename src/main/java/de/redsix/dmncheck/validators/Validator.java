package de.redsix.dmncheck.validators;


import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public interface Validator<T extends ModelElementInstance>{

    boolean isApplicable(final T element);

    List<ValidationResult> validate(final T element);

    Class<T> getClassUnderValidation();

    default List<ValidationResult> apply(final DmnModelInstance dmnModelInstance) {
        final Collection<T> elements = dmnModelInstance.getModelElementsByType(getClassUnderValidation());
        return elements.stream().filter(this::isApplicable).
                flatMap(element -> validate(element).stream()).collect(Collectors.toList());
    }
}
