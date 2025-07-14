package de.redsix.dmncheck.feel;

import java.util.Arrays;
import org.camunda.bpm.model.dmn.instance.ItemDefinition;

public sealed interface ExpressionType {
    record Top() implements ExpressionType {}

    record String() implements ExpressionType {}

    record Boolean() implements ExpressionType {}

    record Integer() implements ExpressionType {}

    record Long() implements ExpressionType {}

    record Double() implements ExpressionType {}

    record Date() implements ExpressionType {}

    record Enum(java.lang.String className) implements ExpressionType {}

    record ItemDefintion(ItemDefinition itemDefinition) implements
        ExpressionType {}

    static boolean isNumeric(final ExpressionType givenType) {
        return (
            !new Top().equals(givenType) &&
            Arrays.asList(new Integer(), new Long(), new Double()).contains(
                givenType
            )
        );
    }

    default boolean isSubtypeOf(final ExpressionType supertype) {
        return (
            reflexivity(this, supertype) ||
            TOPisTopElement(supertype) ||
            INTEGERsubtypeOfLONG(this, supertype) ||
            INTEGERsubtypeOfDOUBLE(this, supertype)
        );
    }

    private boolean reflexivity(
        final ExpressionType subtype,
        final ExpressionType supertype
    ) {
        return subtype.equals(supertype);
    }

    private boolean TOPisTopElement(final ExpressionType supertype) {
        return new Top().equals(supertype);
    }

    private boolean INTEGERsubtypeOfLONG(
        final ExpressionType subtype,
        final ExpressionType supertype
    ) {
        return new Integer().equals(subtype) && new Long().equals(supertype);
    }

    private boolean INTEGERsubtypeOfDOUBLE(
        final ExpressionType subtype,
        final ExpressionType supertype
    ) {
        return new Integer().equals(subtype) && new Double().equals(supertype);
    }
}
