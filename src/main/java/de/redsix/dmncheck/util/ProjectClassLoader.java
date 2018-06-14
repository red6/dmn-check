package de.redsix.dmncheck.util;

public enum ProjectClassLoader {

    instance;

    @SuppressWarnings("nullness")
    public ClassLoader classLoader;
}
