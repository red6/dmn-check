package de.redsix.dmncheck.feel;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
public enum ExpressionType {
    TOP,
    STRING,
    BOOLEAN,
    INTEGER,
    LONG,
    DOUBLE,
    DATE;

    public static boolean isNotValid(final String givenType) {
        return !isValid(givenType);
    }

    public static boolean isValid(final String givenType) {
        return !TOP.name().equalsIgnoreCase(givenType) && Arrays.stream(ExpressionType.values())
                .anyMatch(type -> type.name().equalsIgnoreCase(givenType));
    }

    public static boolean isNumeric(final String givenType) {
        return !TOP.name().equalsIgnoreCase(givenType) && Stream.of(INTEGER, LONG, DOUBLE)
                .anyMatch(type -> type.name().equalsIgnoreCase(givenType));
    }

    public static boolean isNumeric(final ExpressionType givenType) {
        return isNumeric(givenType.name());
    }

    public boolean isSubtypeOf(final ExpressionType supertype) {
        return reflexivity(this, supertype) || TOPisTopElement(supertype) || INTEGERsubtypeOfLONG(this, supertype)
                || INTEGERsubtypeOfDOUBLE(this, supertype);
    }

    private boolean reflexivity(final ExpressionType subtype, final ExpressionType supertype) {
        return subtype.equals(supertype);
    }

    private boolean TOPisTopElement(final ExpressionType supertype) {
        return TOP.equals(supertype);
    }

    private boolean INTEGERsubtypeOfLONG(final ExpressionType subtype, final ExpressionType supertype) {
        return INTEGER.equals(subtype) && LONG.equals(supertype);
    }

    private boolean INTEGERsubtypeOfDOUBLE(final ExpressionType subtype, final ExpressionType supertype) {
        return INTEGER.equals(subtype) && DOUBLE.equals(supertype);
    }

    public static ExpressionType getType(Optional<String> type) {
        return type.map(String::toUpperCase).map(ExpressionType::valueOf).orElse(TOP);
    }
}
