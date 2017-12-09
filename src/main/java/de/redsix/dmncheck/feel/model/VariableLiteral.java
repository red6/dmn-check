package de.redsix.dmncheck.feel.model;

public class VariableLiteral implements FeelExpression {

    public final String name;

    public VariableLiteral(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VariableLiteral that = (VariableLiteral) o;

        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
