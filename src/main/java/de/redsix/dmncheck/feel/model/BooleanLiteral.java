package de.redsix.dmncheck.feel.model;

public class BooleanLiteral implements FeelExpression {

    public final Boolean aBoolean;

    public BooleanLiteral(Boolean aBoolean) {
        this.aBoolean = aBoolean;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BooleanLiteral that = (BooleanLiteral) o;

        return aBoolean != null ? aBoolean.equals(that.aBoolean) : that.aBoolean == null;
    }

    @Override
    public int hashCode() {
        return aBoolean != null ? aBoolean.hashCode() : 0;
    }
}
