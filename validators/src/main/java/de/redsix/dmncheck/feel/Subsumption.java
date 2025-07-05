package de.redsix.dmncheck.feel;

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
                    var aBool
            ) -> switch (otherExpression) {
                case FeelExpression.BooleanLiteral(var anOtherBool) -> Optional.of(comparison.test(aBool, anOtherBool));
                default -> Optional.of(false);
            };

            case FeelExpression.DateLiteral(var dateTime) -> switch (otherExpression) {
                case FeelExpression.DateLiteral(var anOtherDateTime) ->
                        Optional.of(comparison.test(dateTime, anOtherDateTime));
                default -> Optional.of(false);
            };
            case FeelExpression.DoubleLiteral(
                    var aDouble
            ) -> switch (otherExpression) {
                case FeelExpression.DoubleLiteral(var anOtherDouble) ->
                        Optional.of(comparison.test(aDouble, anOtherDouble));
                default -> Optional.of(false);
            };
            case FeelExpression.IntegerLiteral(var anInteger) -> switch (otherExpression) {
                case FeelExpression.IntegerLiteral(var anOtherInteger) ->
                        Optional.of(comparison.test(anInteger, anOtherInteger));
                default -> Optional.of(false);
            };
            case FeelExpression.StringLiteral(
                    var aString
            ) -> switch (otherExpression) {
                case FeelExpression.StringLiteral(var anOtherString) ->
                        Optional.of(comparison.test(aString, anOtherString));
                default -> Optional.of(false);
            };
            case FeelExpression.VariableLiteral(var name) -> subsumesVariableLiteral(name, otherExpression, comparison);
            case FeelExpression.RangeExpression(var leftInc, var lowerBound, var upperBound, var rightInc) ->
                    subsumesRangeExpression(leftInc, lowerBound, upperBound, rightInc, otherExpression);
            case FeelExpression.UnaryExpression(var operator, var operand) ->
                    subsumesUnaryExpression(operator, operand, otherExpression);
            default -> Optional.empty();
        };
    }

    private static Optional<Boolean> subsumesVariableLiteral(
            String name, FeelExpression otherExpression, Comparison comparison) {
        return switch (otherExpression) {
            case FeelExpression.VariableLiteral(var otherName) -> Optional.of(comparison.test(name, otherName));
            case FeelExpression.UnaryExpression(var operator, var operand) ->
                    Operator.NOT.equals(operator) ? subsumesVariableLiteral(name, operand, nq) : Optional.of(true);
            default -> (Optional.of(true));
        };
    }

    private static Optional<Boolean> subsumesUnaryExpression(
            Operator operator, FeelExpression operand, FeelExpression otherExpression) {
        return switch (otherExpression) {
            case FeelExpression.RangeExpression(var leftInc, var lowerBound, var upperBound, var rightInc) ->
                    switch (operator) {
                        case LT -> subsumes(upperBound, operand, rightInc ? lt : le);
                        case GT -> subsumes(operand, lowerBound, leftInc ? lt : le);
                        case LE -> subsumes(upperBound, operand, rightInc ? le : lt);
                        case GE -> subsumes(operand, lowerBound, leftInc ? le : lt);
                        default -> Optional.of(false);
                    };
            case FeelExpression.UnaryExpression(var otherOperator, var otherOperand) -> {
                if (operator.equals(otherOperator) && operand.equals(otherOperand)) {
                    yield Optional.of(true);
                }

                if (operator.isGreaterThan() && otherOperator.isGreaterThan()
                        || operator.isLessThan() && otherOperator.isLessThan()) {
                    yield subsumes(otherOperand, operand, fromOperator(operator));
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
            boolean leftInc,
            FeelExpression lowerBound,
            FeelExpression upperBound,
            boolean rightInc,
            FeelExpression otherExpression) {
        return switch (otherExpression) {
            case FeelExpression.RangeExpression(
                    var __, var otherLowerBound, var otherUpperBound, var ___
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

    private static <R extends Comparable> Optional<Boolean> compareLiterals(
            R value,
            Function<FeelExpression, Optional<R>> extractOtherValue,
            FeelExpression otherExpression,
            Comparison comparison) {
        return Optional.of(extractOtherValue.apply(otherExpression)
                .map(otherValue -> comparison.test(value, otherValue))
                .orElse(false));
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
