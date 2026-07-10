package de.redsix.dmncheck.feel;

public enum Operator {
    ADD("+"),
    SUB("-"),
    MUL("*"),
    DIV("/"),
    EXP("**"),
    LT("<"),
    GT(">"),
    LE("<="),
    GE(">="),
    NOT("NOT"),
    OR("OR"),
    AND("AND"),
    DATE("DATE"),
    DATE_AND_TIME("DATE AND TIME");

    private final String name;

    Operator(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static Operator fromString(final String name) {
        return Operator.valueOf(name.toUpperCase().replace(" ", "_"));
    }

    public boolean isLessThan() {
        return this.equals(LT) || this.equals(LE);
    }

    public boolean isGreaterThan() {
        return this.equals(GT) || this.equals(GE);
    }

    public boolean isDate() {
        return this.equals(DATE) || this.equals(DATE_AND_TIME);
    }
}
