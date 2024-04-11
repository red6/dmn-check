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
    OR("or"),
    AND("and");

    private final String name;

    Operator(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean isLessThan() {
        return this.equals(LT) || this.equals(LE);
    }

    public boolean isGreaterThan() {
        return this.equals(GT) || this.equals(GE);
    }
}
