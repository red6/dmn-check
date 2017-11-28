package de.redsix.dmncheck.validators;


import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public interface GenericValidator<S extends ModelElementInstance, T extends ModelElementInstance>{

    boolean isApplicable(final S element);

    List<ValidationResult> validate(final T element);

    Class<S> getClassUsedToCheckApplicability();

    Class<T> getClassUnderValidation();

    default List<ValidationResult> apply(final DmnModelInstance dmnModelInstance) {
        final Collection<S> elements = dmnModelInstance.getModelElementsByType(getClassUsedToCheckApplicability());
        return elements.stream().filter(this::isApplicable)
                .flatMap(element -> element.getParentElement().getChildElementsByType(getClassUnderValidation()).stream())
                .flatMap(element -> validate(element).stream()).collect(Collectors.toList());
    }
}
