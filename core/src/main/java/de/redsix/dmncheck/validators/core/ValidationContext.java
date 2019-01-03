package de.redsix.dmncheck.validators.core;

import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.instance.ItemDefinition;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class ValidationContext {

    private class Memoizer<T, U> {

        private final Map<Class<T>, U> cache = new ConcurrentHashMap<>();

        private Function<Class<T>, U> doMemoize(final Function<Class<T>, U> function) {
            return input -> cache.computeIfAbsent(input, function);
        }

    }

    private final Function<Class<ItemDefinition>, Collection<ItemDefinition>> itemDefinitions;


    public ValidationContext(final DmnModelInstance dmnModelInstance) {
        this.itemDefinitions = new Memoizer<ItemDefinition, Collection<ItemDefinition>>()
                .doMemoize(dmnModelInstance::getModelElementsByType);
    }

    public Collection<ItemDefinition> getItemDefinitions() {
        return itemDefinitions.apply(ItemDefinition.class);
    }

}
