package de.redsix.dmncheck.util;

import org.checkerframework.checker.nullness.qual.Nullable;

public enum ProjectClassLoader {

    INSTANCE;

    public @Nullable ClassLoader classLoader;
}
