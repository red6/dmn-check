package de.redsix.dmncheck.feel;

import org.camunda.bpm.model.dmn.instance.ItemDefinition;

import java.util.Arrays;

public sealed interface ExpressionType {

    record TOP() implements ExpressionType {
    }

    record STRING() implements ExpressionType {
    }

    record BOOLEAN() implements ExpressionType {
    }

    record INTEGER() implements ExpressionType {
    }
    record LONG() implements ExpressionType {
    }

    record DOUBLE() implements ExpressionType {
    }

    record DATE() implements ExpressionType {
    }

    record ENUM(String className) implements ExpressionType {
    }

    record ITEMDEFINITION(ItemDefinition itemDefinition) implements ExpressionType {
    }

    static boolean isNumeric(final ExpressionType givenType) {
        return !new TOP().equals(givenType)
                && Arrays.asList(new INTEGER(), new LONG(), new DOUBLE()).contains(givenType);
    }

    default boolean isSubtypeOf(final ExpressionType supertype) {
        return reflexivity(this, supertype)
                || TOPisTopElement(supertype)
                || INTEGERsubtypeOfLONG(this, supertype)
                || INTEGERsubtypeOfDOUBLE(this, supertype);
    }

    private boolean reflexivity(final ExpressionType subtype, final ExpressionType supertype) {
        return subtype.equals(supertype);
    }

    private boolean TOPisTopElement(final ExpressionType supertype) {
        return new TOP().equals(supertype);
    }

    private boolean INTEGERsubtypeOfLONG(final ExpressionType subtype, final ExpressionType supertype) {
        return new INTEGER().equals(subtype) && new LONG().equals(supertype);
    }

    private boolean INTEGERsubtypeOfDOUBLE(final ExpressionType subtype, final ExpressionType supertype) {
        return new INTEGER().equals(subtype) && new DOUBLE().equals(supertype);
    }
}
