package de.redsix.dmncheck.feel.model;

public class UnaryExpression implements FeelExpression {

    public final Operator operator;
    public final FeelExpression expression;

    public UnaryExpression(Operator operator, FeelExpression expression) {
        this.operator = operator;
        this.expression = expression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        UnaryExpression that = (UnaryExpression) o;

        if (operator != that.operator)
            return false;
        return expression != null ? expression.equals(that.expression) : that.expression == null;
    }

    @Override
    public int hashCode() {
        int result = operator != null ? operator.hashCode() : 0;
        result = 31 * result + (expression != null ? expression.hashCode() : 0);
        return result;
    }
}
