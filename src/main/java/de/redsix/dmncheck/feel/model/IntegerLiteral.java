package de.redsix.dmncheck.feel.model;

public class IntegerLiteral implements FeelExpression {

    public final Integer integer;

    public IntegerLiteral(Integer integer) {
        this.integer = integer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        IntegerLiteral that = (IntegerLiteral) o;

        return integer != null ? integer.equals(that.integer) : that.integer == null;
    }

    @Override
    public int hashCode() {
        return integer != null ? integer.hashCode() : 0;
    }
}
