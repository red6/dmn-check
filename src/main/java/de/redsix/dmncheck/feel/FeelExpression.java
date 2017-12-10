package de.redsix.dmncheck.feel;

import org.derive4j.Data;

import java.time.LocalDateTime;

@Data
public abstract class FeelExpression {

    public interface Cases<R> {
        R BooleanLiteral(Boolean aBoolean);
        R DateLiteral(LocalDateTime dateTime);
        R DisjunctionExpression(FeelExpression head, FeelExpression tail);
        R DoubleLiteral(Double aDouble);
        R IntegerLiteral(Integer aInteger);
        R RangeExpression(boolean isLeftInclusive, FeelExpression lowerBound, FeelExpression upperBound, boolean isRightInclusive);
        R StringLiteral(String string);
        R UnaryExpression(Operator operator, FeelExpression expression);
        R BinaryExpression(FeelExpression left, Operator operator, FeelExpression right);
        R VariableLiteral(String name);
    }

    public abstract <R> R match(Cases<R> cases);

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract String toString();
}
