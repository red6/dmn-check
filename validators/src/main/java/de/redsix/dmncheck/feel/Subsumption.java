package de.redsix.dmncheck.feel;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;

final class Subsumption {

    private interface Comparison<A extends Comparable> extends BiPredicate<A, A> {}

    static final Comparison<?> eq = (a, b) -> a.compareTo(b) == 0;
    private static final Comparison<?> nq = (a, b) -> a.compareTo(b) != 0;
    private static final Comparison<?> gt = (a, b) -> a.compareTo(b) > 0;
    private static final Comparison<?> lt = (a, b) -> a.compareTo(b) < 0;
    private static final Comparison<?> ge = (a, b) -> a.compareTo(b) == 0 ||  a.compareTo(b) > 0;
    private static final Comparison<?> le = (a, b) -> a.compareTo(b) == 0 ||  a.compareTo(b) < 0;

    static Optional<Boolean> subsumes(final FeelExpression expression, final FeelExpression otherExpression, final Comparison comparison) {
        return FeelExpressions.caseOf(expression)
                .Empty_(Optional.of(true))
                .Null_(FeelExpressions.caseOf(otherExpression).Null_(Optional.of(true)).otherwise_(Optional.of(false)))
                .BooleanLiteral((aBool) -> compareLiterals(aBool, FeelExpressions::getABoolean, otherExpression, comparison))
                .DateLiteral((dateTime) -> compareLiterals(dateTime, FeelExpressions::getDateTime, otherExpression, comparison))
                .DoubleLiteral((aDouble) -> compareLiterals(aDouble, FeelExpressions::getADouble, otherExpression, comparison))
                .IntegerLiteral((integer) -> compareLiterals(integer, FeelExpressions::getAInteger, otherExpression, comparison))
                .StringLiteral((string) ->  compareLiterals(string, FeelExpressions::getString, otherExpression, eq))
                .VariableLiteral_(Optional.of(true))
                .RangeExpression((leftInc, lowerBound, upperBound, rightInc) ->
                        subsumesRangeExpression(leftInc, lowerBound, upperBound, rightInc, otherExpression))
                .UnaryExpression((operator, operand) -> subsumesUnaryExpression(operator, operand, otherExpression))
                .otherwise_(Optional.empty());
    }

    private static Optional<Boolean> subsumesUnaryExpression(Operator operator, FeelExpression operand, FeelExpression otherExpression) {
        return FeelExpressions.caseOf(otherExpression)
                .RangeExpression((leftInc, lowerBound, upperBound, rightInc) -> {
                    switch (operator) {
                        case LT: return subsumes(upperBound, operand, rightInc ? lt : le);
                        case GT: return subsumes(operand, lowerBound, leftInc ? lt : le);
                        case LE: return subsumes(upperBound, operand, rightInc ? le : lt);
                        case GE: return subsumes(operand, lowerBound, leftInc ? le : lt);
                        default: return Optional.of(false);
                    }
                })
                .UnaryExpression((otherOperator, otherOperand) -> {
                    if (operator.equals(otherOperator) && operand.equals(otherOperand)) {
                        return Optional.of(true);
                    }
                    if (isGreater(fromOperator(operator)) && isGreater(fromOperator(otherOperator))) {
                        return subsumes(otherOperand, operand, fromOperator(operator));
                    } else if (isLess(fromOperator(operator)) && isLess(fromOperator(otherOperator))) {
                        return subsumes(otherOperand, operand, fromOperator(operator));
                    } else {
                        return Optional.of(false);
                    }
                })
                .otherwise(() -> {
                    if (operator.equals(Operator.NOT) && otherExpression.isLiteral()) {
                        return subsumes(operand, otherExpression, nq);
                    } else {
                        return Optional.of(false);
                    }
                });
    }

    private static Optional<Boolean> subsumesRangeExpression(boolean leftInc, FeelExpression lowerBound, FeelExpression upperBound,
            boolean rightInc, FeelExpression otherExpression) {
        return FeelExpressions.caseOf(otherExpression)
                .RangeExpression((otherLeftInc, otherLowerBound, otherUpperBound, otherRightInc) ->
                        subsumes(lowerBound, otherLowerBound, leftInc ? le : lt).flatMap(
                                subsumesLowerBound ->
                                        subsumes(otherUpperBound, upperBound, rightInc ? le : lt).flatMap(
                                                subsumesUpperBound ->
                                                        Optional.of(subsumesLowerBound && subsumesUpperBound))))
                .otherwise(() -> {
                    if (otherExpression.isLiteral()) {
                        return subsumes(lowerBound, otherExpression, leftInc ? le : lt).flatMap(
                                subsumedByLowerBound -> subsumes(upperBound, otherExpression, rightInc ? ge : gt).flatMap(
                                        subsumedByUpperBound -> Optional.of(subsumedByLowerBound && subsumedByUpperBound)
                                ));
                    } else {
                        return Optional.of(false);
                    }
                });
    }

    private static <R extends Comparable> Optional<Boolean> compareLiterals(R value,
            Function<FeelExpression, Optional<R>> extractOtherValue, FeelExpression otherExpression, Comparison comparison) {
        return Optional.of(extractOtherValue.apply(otherExpression).map(otherValue -> comparison.test(value, otherValue)).orElse(false));
    }

    private static Comparison fromOperator(final Operator operator) {
        switch (operator) {
            case LE: return le;
            case LT: return lt;
            case GT: return gt;
            case GE: return ge;
            default: return eq;
        }
    }

    private static boolean isLess(final Comparison comparison) {
        return comparison.equals(le) ||comparison.equals(lt) ;
    }

    private static boolean isGreater(final Comparison comparison) {
        return comparison.equals(ge) ||comparison.equals(gt) ;
    }
}
