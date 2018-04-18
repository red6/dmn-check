package de.redsix.dmncheck.feel;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
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
}
