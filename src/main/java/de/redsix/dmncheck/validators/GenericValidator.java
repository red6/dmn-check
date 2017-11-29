package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.result.ValidationResult;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.type.ModelElementType;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface GenericValidator<S extends ModelElementInstance, T extends ModelElementInstance>{

    boolean isApplicable(final S element);

    List<ValidationResult> validate(final T element);

    Class<S> getClassUsedToCheckApplicability();

    Class<T> getClassUnderValidation();

    default List<ValidationResult> apply(final DmnModelInstance dmnModelInstance) {
        final ModelElementType elementType = dmnModelInstance.getModel().getType(getClassUnderValidation());
        final Collection<S> elements = dmnModelInstance.getModelElementsByType(getClassUsedToCheckApplicability());
        return elements.stream().filter(this::isApplicable).flatMap(element -> Stream
                .concat(element.getChildElementsByType(getClassUnderValidation()).stream(),
                        element.getElementType().equals(elementType) ? Stream.of((T) element) : Stream.empty()))
                .flatMap(element -> validate(element).stream()).collect(Collectors.toList());
    }
}
