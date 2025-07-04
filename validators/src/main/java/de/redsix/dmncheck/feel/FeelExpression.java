package de.redsix.dmncheck.feel;

import java.time.LocalDateTime;
import java.util.Optional;

public sealed interface FeelExpression {
    record Empty() implements FeelExpression {
    }

    record Null() implements FeelExpression {
    }

    record BooleanLiteral(Boolean aBoolean) implements FeelExpression {
    }

    record DateLiteral(LocalDateTime dateTime) implements FeelExpression {
    }

    record DoubleLiteral(Double aDouble) implements FeelExpression {
    }

    record IntegerLiteral(Integer aInteger) implements FeelExpression {
    }

    record StringLiteral(String string) implements FeelExpression {
    }

    record VariableLiteral(String name) implements FeelExpression {
    }

    record RangeExpression(boolean isLeftInclusive, FeelExpression lowerBound, FeelExpression upperBound,
                           boolean isRightInclusive) implements FeelExpression {
    }

    record UnaryExpression(Operator operator, FeelExpression expression) implements FeelExpression {
    }

    record BinaryExpression(FeelExpression left, Operator operator, FeelExpression right) implements FeelExpression {
    }

    record DisjunctionExpression(FeelExpression head, FeelExpression tail) implements FeelExpression {
    }

    default Optional<Boolean> subsumes(final FeelExpression expression) {
        return Subsumption.subsumes(this, expression, Subsumption.eq);
    }

    default boolean containsVariable(final String name) {
        return switch (this) {
            case Empty() -> false;
            case Null() -> false;
            case BooleanLiteral(var __) -> false;
            case DateLiteral(var __) -> false;
            case DoubleLiteral(var __) -> false;
            case IntegerLiteral(var __) -> false;
            case StringLiteral(var __) -> false;
            case VariableLiteral(var variableName) -> variableName.equals(name);
            case RangeExpression(var __, var lowerBound, var upperBound, var ___) ->
                    lowerBound.containsVariable(name) || upperBound.containsVariable(name);
            case UnaryExpression(var __, var expression) -> expression.containsVariable(name);
            case BinaryExpression(var left, var __, var right) ->
                    left.containsVariable(name) || right.containsVariable(name);
            case DisjunctionExpression(var head, var tail) ->
                    head.containsVariable(name) || tail.containsVariable(name);
        };
    }

    default boolean isLiteral() {
        return switch (this) {
            case BooleanLiteral(var __) -> true;
            case DateLiteral(var __) -> true;
            case DoubleLiteral(var __) -> true;
            case IntegerLiteral(var __) -> true;
            case VariableLiteral(var __) -> true;
            default -> false;
        };
    }

    default boolean containsNot() {
        return switch (this) {
            case Empty() -> false;
            case Null() -> false;
            case BooleanLiteral(var __) -> false;
            case DateLiteral(var __) -> false;
            case DoubleLiteral(var __) -> false;
            case IntegerLiteral(var __) -> false;
            case StringLiteral(var __) -> false;
            case VariableLiteral(var __) -> false;
            case RangeExpression(var __, var lowerBound, var upperBound, var ___) ->
                    lowerBound.containsNot() || upperBound.containsNot();
            case UnaryExpression(var operator, var __) -> operator.equals(Operator.NOT);
            case BinaryExpression(var left, var __, var right) -> left.containsNot() || right.containsNot();
            case DisjunctionExpression(var head, var tail) -> head.containsNot() || tail.containsNot();
        };
    }
}
