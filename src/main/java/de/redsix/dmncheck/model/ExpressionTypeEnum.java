package de.redsix.dmncheck.model;

import java.util.Arrays;
import java.util.stream.Stream;

public enum ExpressionTypeEnum {
    STRING,
    BOOLEAN,
    INTEGER,
    LONG,
    DOUBLE,
    DATE;

    public static boolean isValid(final String givenType) {
        return Arrays.stream(ExpressionTypeEnum.values()).noneMatch(type -> type.name().equalsIgnoreCase(givenType));
    }

    public static boolean isNumeric(final String givenType) {
        return Stream.of(INTEGER, LONG, DOUBLE).anyMatch(type -> type.name().equalsIgnoreCase(givenType));
    }

}