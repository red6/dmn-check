package de.redsix.dmncheck.feel.model;

public class BinaryExpression implements FeelExpression {
    public final FeelExpression left;
    public final Operator op;
    public final FeelExpression right;

    public BinaryExpression(FeelExpression left, Operator op, FeelExpression right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        BinaryExpression that = (BinaryExpression) o;

        if (left != null ? !left.equals(that.left) : that.left != null)
            return false;
        if (op != that.op)
            return false;
        return right != null ? right.equals(that.right) : that.right == null;
    }

    @Override
    public int hashCode() {
        int result = left != null ? left.hashCode() : 0;
        result = 31 * result + (op != null ? op.hashCode() : 0);
        result = 31 * result + (right != null ? right.hashCode() : 0);
        return result;
    }
}
