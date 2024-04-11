package de.redsix.dmncheck.validators.core;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.instance.ItemDefinition;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * The validation context is used in validators with a local view of the DMN model instance to provide access to global
 * attributes of the DMN model instance.
 *
 * <p>Note: Reading from the validation context is cached.
 */
public class ValidationContext {

    private static class Memoizer<T, U> {

        private final Map<Class<T>, U> cache = new ConcurrentHashMap<>();

        private Function<Class<T>, U> doMemoize(final Function<Class<T>, @NonNull U> function) {
            return input -> cache.computeIfAbsent(input, function);
        }
    }

    private final Function<Class<ItemDefinition>, Collection<ItemDefinition>> itemDefinitions;

    public ValidationContext(final DmnModelInstance dmnModelInstance) {
        this.itemDefinitions = new Memoizer<ItemDefinition, Collection<ItemDefinition>>()
                .doMemoize(dmnModelInstance::getModelElementsByType);
    }

    /**
     * Provides access to the item definitions of a DMN model instance.
     *
     * @return A list of item definitions
     */
    public Collection<ItemDefinition> getItemDefinitions() {
        return itemDefinitions.apply(ItemDefinition.class);
    }
}
