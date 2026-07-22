package de.redsix.dmncheck.result;

/** The severity of a validation result. */
public enum Severity {
    WARNING,
    ERROR;

    public boolean isError() {
        return this.equals(ERROR);
    }
}
