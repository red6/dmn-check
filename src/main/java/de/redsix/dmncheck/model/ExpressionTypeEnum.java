package de.redsix.dmncheck.model;

import java.util.Arrays;

public enum ExpressionTypeEnum {
    STRING,
    BOOLEAN,
    INTEGR,
    LONG,
    DOUBLE,
    DATE;

    public static boolean isValid(final String givenType) {
        return Arrays.stream(ExpressionTypeEnum.values()).noneMatch(type -> type.name().equalsIgnoreCase(givenType));
    }
}
