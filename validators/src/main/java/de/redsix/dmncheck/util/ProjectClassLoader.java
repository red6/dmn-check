package de.redsix.dmncheck.util;

import org.checkerframework.checker.nullness.qual.Nullable;

public enum ProjectClassLoader {

    instance;

    public @Nullable ClassLoader classLoader;
}
