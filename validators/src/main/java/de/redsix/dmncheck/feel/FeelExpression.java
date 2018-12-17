package de.redsix.dmncheck.feel;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.derive4j.Data;
import org.derive4j.Derive;
import org.derive4j.Make;
import org.derive4j.Visibility;

import java.time.LocalDateTime;
import java.util.Optional;

@Data(value = @Derive(withVisibility = Visibility.Package, make = {Make.constructors, Make.caseOfMatching, Make.getters}))
public abstract class FeelExpression {

    public interface Cases<R> {
        R Empty();
        R BooleanLiteral(Boolean aBoolean);
        R DateLiteral(LocalDateTime dateTime);
        R DoubleLiteral(Double aDouble);
        R IntegerLiteral(Integer aInteger);
        R StringLiteral(String string);
        R VariableLiteral(String name);
        R RangeExpression(boolean isLeftInclusive, FeelExpression lowerBound, FeelExpression upperBound, boolean isRightInclusive);
        R UnaryExpression(Operator operator, FeelExpression expression);
        R BinaryExpression(FeelExpression left, Operator operator, FeelExpression right);
        R DisjunctionExpression(FeelExpression head, FeelExpression tail);
    }

    public abstract <R> R match(Cases<R> cases);

    public Optional<Boolean> subsumes(final FeelExpression expression) {
        return Subsumption.subsumes(this, expression, Subsumption.eq);
    }

    public boolean containsVariable(final String name) {
        return FeelExpressions.caseOf(this)
                .Empty_(false)
                .BooleanLiteral_(false)
                .DateLiteral_(false)
                .DoubleLiteral_(false)
                .IntegerLiteral_(false)
                .StringLiteral_(false)
                .VariableLiteral(variableName -> variableName.equals(name))
                .RangeExpression((__, lowerBound, upperBound, ___) -> lowerBound.containsVariable(name) || upperBound.containsVariable(name))
                .UnaryExpression((__, expression) -> expression.containsVariable(name))
                .BinaryExpression((left, __, right) -> left.containsVariable(name) || right.containsVariable(name))
                .DisjunctionExpression((head, tail) -> head.containsVariable(name) || tail.containsVariable(name));
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(@Nullable Object obj);

    @Override
    public abstract String toString();
}
