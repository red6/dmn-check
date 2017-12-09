package de.redsix.dmncheck.feel.model;

public class DisjunctionExpression implements FeelExpression {

    public final FeelExpression head;
    public final FeelExpression tail;

    public DisjunctionExpression(FeelExpression head, FeelExpression tail) {
        this.head = head;
        this.tail = tail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        DisjunctionExpression that = (DisjunctionExpression) o;

        if (head != null ? !head.equals(that.head) : that.head != null)
            return false;
        return tail != null ? tail.equals(that.tail) : that.tail == null;
    }

    @Override
    public int hashCode() {
        int result = head != null ? head.hashCode() : 0;
        result = 31 * result + (tail != null ? tail.hashCode() : 0);
        return result;
    }
}
