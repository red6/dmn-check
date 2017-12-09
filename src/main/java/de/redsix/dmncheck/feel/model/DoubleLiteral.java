package de.redsix.dmncheck.feel.model;

public class DoubleLiteral implements FeelExpression {

    public final Double aDouble;

    public DoubleLiteral(Double aDouble) {
        this.aDouble = aDouble;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DoubleLiteral that = (DoubleLiteral) o;

        return aDouble != null ? aDouble.equals(that.aDouble) : that.aDouble == null;
    }

    @Override
    public int hashCode() {
        return aDouble != null ? aDouble.hashCode() : 0;
    }
}
