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

    record QuestionMark() implements FeelExpression {
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
            case QuestionMark() -> true;
            case BooleanLiteral(final var __) -> false;
            case DateLiteral(final var __) -> false;
            case DateTimeLiteral(final var __) -> false;
            case DoubleLiteral(final var __) -> false;
            case IntegerLiteral(final var __) -> false;
            case StringLiteral(final var __) -> false;
            case VariableLiteral(final var variableName) -> variableName.equals(name);
            case RangeExpression(final var __, final var lowerBound, final var upperBound, final var ___) ->
                    lowerBound.containsVariable(name) || upperBound.containsVariable(name);
            case NaryExpression(final var __, final var operands) -> operands.stream().anyMatch(operand -> operand.containsVariable(name));
            case DisjunctionExpression(final var head, final var tail) ->
                    head.containsVariable(name) || tail.containsVariable(name);
        };
    }

    default boolean isLiteral() {
        return switch (this) {
            case QuestionMark() -> true;
            case BooleanLiteral(final var __) -> true;
            case DateLiteral(final var __) -> true;
            case DateTimeLiteral(final var __) -> true;
            case DoubleLiteral(final var __) -> true;
            case IntegerLiteral(final var __) -> true;
            case VariableLiteral(final var __) -> true;
            default -> false;
        };
    }

    default boolean isDate() {
        return switch (this) {
            case NaryExpression(final var operator, final var operands) -> operator.isDate() && operands.size() == 1;
            default -> false;
        };
    }

    default FeelExpression extractDateExpression() {
        return switch (this) {
            case NaryExpression(final var operator, final var operands) -> operator.isDate() ? operands.getFirst() : this;
            default -> this;
        };
    }

    static FeelExpression unaryExpression(final Operator operator, final FeelExpression operand) {
        return new NaryExpression(operator, List.of(operand));
    }

    static FeelExpression binaryExpression(final Operator operator, final FeelExpression operand, final FeelExpression otherOperand) {
        return new NaryExpression(operator, List.of(operand, otherOperand));
    }
}
