package de.redsix.dmncheck.feel;

import de.redsix.dmncheck.feel.FeelExpression.DateLiteral;
import de.redsix.dmncheck.feel.FeelExpression.DateTimeLiteral;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;

final class Subsumption {

    private interface Comparison<A extends Comparable> extends BiPredicate<A, A> {
    }

    static final Comparison<?> eq = (a, b) -> a.compareTo(b) == 0;
    private static final Comparison<?> nq = (a, b) -> a.compareTo(b) != 0;
    private static final Comparison<?> gt = (a, b) -> a.compareTo(b) > 0;
    private static final Comparison<?> lt = (a, b) -> a.compareTo(b) < 0;
    private static final Comparison<?> ge = (a, b) -> a.compareTo(b) == 0 || a.compareTo(b) > 0;
    private static final Comparison<?> le = (a, b) -> a.compareTo(b) == 0 || a.compareTo(b) < 0;

    static Optional<Boolean> subsumes(
            final FeelExpression expression, final FeelExpression otherExpression, final Comparison comparison) {
        return switch (expression) {
            case FeelExpression.Empty() -> Optional.of(true);
            case FeelExpression.Null() -> switch (otherExpression) {
                case FeelExpression.Null() -> Optional.of(true);
                default -> Optional.of(false);
            };
            case FeelExpression.BooleanLiteral(
                final var aBool
            ) -> switch (otherExpression) {
                case FeelExpression.BooleanLiteral(final var anOtherBool) -> Optional.of(comparison.test(aBool, anOtherBool));
                default -> Optional.of(false);
            };

            case DateLiteral(final var dateTime) -> switch (otherExpression) {
                case DateLiteral(final var anOtherDate) ->
                    Optional.of(comparison.test(dateTime, anOtherDate));
                default -> Optional.of(false);
            };

            case DateTimeLiteral(final var dateTime) -> switch (otherExpression) {
                case DateTimeLiteral(final var anOtherDateTime) ->
                        Optional.of(comparison.test(dateTime, anOtherDateTime));
                default -> Optional.of(false);
            };

            case FeelExpression.DoubleLiteral(
                final var aDouble
            ) -> switch (otherExpression) {
                case FeelExpression.DoubleLiteral(final var anOtherDouble) ->
                        Optional.of(comparison.test(aDouble, anOtherDouble));
                default -> Optional.of(false);
            };
            case FeelExpression.IntegerLiteral(final var anInteger) -> switch (otherExpression) {
                case FeelExpression.IntegerLiteral(final var anOtherInteger) ->
                        Optional.of(comparison.test(anInteger, anOtherInteger));
                default -> Optional.of(false);
            };
            case FeelExpression.StringLiteral(
                final var aString
            ) -> switch (otherExpression) {
                case FeelExpression.StringLiteral(final var anOtherString) ->
                        Optional.of(comparison.test(aString, anOtherString));
                default -> Optional.of(false);
            };
            case FeelExpression.VariableLiteral(final var name) -> subsumesVariableLiteral(name, otherExpression, comparison);
            case FeelExpression.RangeExpression(final var leftInc, final var lowerBound, final var upperBound, final var rightInc) ->
                    subsumesRangeExpression(leftInc, lowerBound, upperBound, rightInc, otherExpression);
            case FeelExpression.NaryExpression(final var operator, final var operands) ->
                    subsumesNaryExpression(operator, operands, otherExpression);
            default -> Optional.empty();
        };
    }

    private static Optional<Boolean> subsumesVariableLiteral(
            final String name, final FeelExpression otherExpression, final Comparison comparison) {
        return switch (otherExpression) {
            case FeelExpression.VariableLiteral(final var otherName) -> Optional.of(comparison.test(name, otherName));
            case FeelExpression.NaryExpression(final var operator, final var operands) -> {
                if (operands.size() != 1) {
                    yield Optional.of(false);
                } else {
                    final var operand = operands.getFirst();
                    yield Operator.NOT.equals(operator) ? subsumesVariableLiteral(name, operand, nq) : Optional.of(true);
                }
            }
            default -> (Optional.of(true));
        };
    }

    private static Optional<Boolean> subsumesNaryExpression(
            final Operator operator, final List<FeelExpression> operands, final FeelExpression otherExpression) {
        if (operands.size() != 1) {
            return Optional.of(false);
        }

        final var operand = operands.getFirst();

        return switch (otherExpression) {
            case FeelExpression.RangeExpression(final var leftInc, final var lowerBound, final var upperBound, final var rightInc) ->
                    switch (operator) {
                        case LT -> subsumes(upperBound, operand, rightInc ? lt : le);
                        case GT -> subsumes(operand, lowerBound, leftInc ? lt : le);
                        case LE -> subsumes(upperBound, operand, rightInc ? le : lt);
                        case GE -> subsumes(operand, lowerBound, leftInc ? le : lt);
                        default -> Optional.of(false);
                    };
            case FeelExpression.NaryExpression(final var otherOperator, final var otherOperands) -> {
                if (otherOperands.size() != 1) {
                    yield Optional.of(false);
                }
                final var otherOperand = otherOperands.getFirst();
                if (operator.equals(otherOperator) && operand.equals(otherOperand)) {
                    yield Optional.of(true);
                }

                if (operator.isGreaterThan() && otherOperator.isGreaterThan()
                        || operator.isLessThan() && otherOperator.isLessThan()) {
                    if (operand.isDate() && otherOperand.isDate()) {
                        yield subsumes(otherOperand.extractDateExpression(), operand.extractDateExpression(), fromOperator(operator));
                    } else {
                        yield subsumes(otherOperand, operand, fromOperator(operator));
                    }
                } else {
                    yield Optional.of(false);
                }
            }
            default -> {
                if (operator.equals(Operator.NOT) && otherExpression.isLiteral()) {
                    yield subsumes(operand, otherExpression, nq);
                } else {
                    yield Optional.of(false);
                }
            }
        };
    }

    private static Optional<Boolean> subsumesRangeExpression(
            final boolean leftInc,
            final FeelExpression lowerBound,
            final FeelExpression upperBound,
            final boolean rightInc,
            final FeelExpression otherExpression) {
        return switch (otherExpression) {
            case FeelExpression.RangeExpression(
                final var __, final var otherLowerBound, final var otherUpperBound, final var ___
            ) -> subsumes(
                    lowerBound, otherLowerBound, leftInc ? le : lt)
                    .flatMap(subsumesLowerBound -> subsumes(otherUpperBound, upperBound, rightInc ? le : lt)
                            .flatMap(subsumesUpperBound -> Optional.of(subsumesLowerBound && subsumesUpperBound)));
            default -> {
                if (otherExpression.isLiteral()) {
                    yield subsumes(lowerBound, otherExpression, leftInc ? le : lt)
                            .flatMap(subsumedByLowerBound -> subsumes(
                                    upperBound, otherExpression, rightInc ? ge : gt)
                                    .flatMap(subsumedByUpperBound ->
                                            Optional.of(subsumedByLowerBound && subsumedByUpperBound)));
                } else {
                    yield Optional.of(false);
                }
            }
        };
    }

    private static Comparison fromOperator(final Operator operator) {
        return switch (operator) {
            case LE -> le;
            case LT -> lt;
            case GT -> gt;
            case GE -> ge;
            default -> eq;
        };
    }
}
