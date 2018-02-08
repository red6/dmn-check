package de.redsix.dmncheck.feel;

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

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract String toString();
}
