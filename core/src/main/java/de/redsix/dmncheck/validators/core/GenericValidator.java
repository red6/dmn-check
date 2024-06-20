package de.redsix.dmncheck.validators.core;

import de.redsix.dmncheck.result.ValidationResult;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

/**
 * Generic validator that facilitates writing validations directly on the structure of the DMN model instance.
 *
 * <p>Note: We are using the Camunda implementation of the DMN model (<a
 * href="https://github.com/camunda/camunda-dmn-model">...</a>).
 *
 * <p>To write a validation using this validator you have to specify:
 *
 * <p>1) The element type (T) that is validated 2) The element type (S) that is used to check whether the validation is
 * applicable
 *
 * <p>The validator collects all elements of type S and checks whether the validation is applicable. It then applies the
 * validation on all child elements of type T.
 *
 * @param <S> Class used to check whether the validation is applicable
 * @param <T> Class used for validation
 */
public abstract class GenericValidator<S extends ModelElementInstance, T extends ModelElementInstance>
        implements Validator {

    /**
     * Checks whether the validation is applicable for an element of type S taking account of the validation context.
     *
     * @param element Element used to check applicability of the validation
     * @param validationContext Validation Context
     * @return Whether the validation is applicable
     */
    protected abstract boolean isApplicable(final S element, ValidationContext validationContext);

    /**
     * Validates a given element of type T.
     *
     * <p>A validation context can be used to track global assumptions about the DMN model instance.
     *
     * @param element Element under validation
     * @param validationContext Validation Context
     * @return A list of validation results
     */
    protected abstract List<ValidationResult> validate(final T element, ValidationContext validationContext);

    /**
     * Auxiliary method to determine the class used for the applicability check.
     *
     * @return Class used to check whether the validation is applicable
     */
    protected abstract Class<S> getClassUsedToCheckApplicability();

    /**
     * Auxiliary method to determine the class for the validation.
     *
     * @return Class used for validation
     */
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
        final Stream<T> childElementsUnderValidation =
                element.getChildElementsByType(getClassUnderValidation()).stream();

        if (getClassUnderValidation().isInstance(element)) {
            return Stream.concat(childElementsUnderValidation, Stream.of((T) element));
        } else {
            return childElementsUnderValidation;
        }
    }
}
