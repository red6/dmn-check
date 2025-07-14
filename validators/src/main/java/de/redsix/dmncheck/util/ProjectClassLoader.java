package de.redsix.dmncheck.util;

import org.checkerframework.checker.nullness.qual.Nullable;

public enum ProjectClassLoader {
    INSTANCE;

    @Nullable
    public ClassLoader classLoader;
}
