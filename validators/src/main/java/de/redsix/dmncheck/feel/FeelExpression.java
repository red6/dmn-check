package de.redsix.dmncheck.feel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public sealed interface FeelExpression {
    record Empty() implements FeelExpression {
    }

    record Null() implements FeelExpression {
    }

    record BooleanLiteral(Boolean aBoolean) implements FeelExpression {
    }

    record DateLiteral(LocalDate dateTime) implements FeelExpression {
    }

    record DateTimeLiteral(LocalDateTime dateTime) implements FeelExpression {
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

    record NaryExpression(Operator operator, List<FeelExpression> arguments) implements FeelExpression {
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
            case DateTimeLiteral(var __) -> false;
            case DoubleLiteral(var __) -> false;
            case IntegerLiteral(var __) -> false;
            case StringLiteral(var __) -> false;
            case VariableLiteral(var variableName) -> variableName.equals(name);
            case RangeExpression(var __, var lowerBound, var upperBound, var ___) ->
                    lowerBound.containsVariable(name) || upperBound.containsVariable(name);
            case NaryExpression(var __, var operands) -> operands.stream().anyMatch(operand -> operand.containsVariable(name));
            case DisjunctionExpression(var head, var tail) ->
                    head.containsVariable(name) || tail.containsVariable(name);
        };
    }

    default boolean isLiteral() {
        return switch (this) {
            case BooleanLiteral(var __) -> true;
            case DateLiteral(var __) -> true;
            case DateTimeLiteral(var __) -> true;
            case DoubleLiteral(var __) -> true;
            case IntegerLiteral(var __) -> true;
            case VariableLiteral(var __) -> true;
            default -> false;
        };
    }

    default boolean isDate() {
        return switch (this) {
            case NaryExpression(var operator, var operands) -> operator.isDate() && operands.size() == 1;
            default -> false;
        };
    }

    default FeelExpression extractDateExpression() {
        return switch (this) {
            case NaryExpression(var operator, var operands) -> operator.isDate() ? operands.getFirst() : this;
            default -> this;
        };
    }

    static FeelExpression unaryExpression(Operator operator, FeelExpression operand) {
        return new NaryExpression(operator, List.of(operand));
    }

    static FeelExpression binaryExpression(Operator operator, FeelExpression operand, FeelExpression otherOperand) {
        return new NaryExpression(operator, List.of(operand, otherOperand));
    }
}
