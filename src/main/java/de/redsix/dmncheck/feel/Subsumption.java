package de.redsix.dmncheck.feel;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

class Subsumption {

    static Optional<Boolean> subsumes(final FeelExpression expression, final FeelExpression otherExpression, final Comparison comparison) {
        return FeelExpressions.caseOf(expression)
                .Empty(() -> Optional.of(true))
                .BooleanLiteral(
                        (aBool) -> FeelExpressions.getABoolean(otherExpression).map(otherBool -> Optional.of(comparison.apply(aBool, otherBool)))
                                .orElse(Optional.of(false)))
                .DateLiteral((dateTime) -> FeelExpressions.getDateTime(otherExpression)
                        .map(otherDateTime -> Optional.of(comparison.apply(dateTime, otherDateTime))).orElse(Optional.of(false)))
                .DoubleLiteral((aDouble) -> FeelExpressions.getADouble(otherExpression)
                        .map(otherDouble -> Optional.of(comparison.apply(aDouble, otherDouble))).orElse(Optional.of(false)))
                .IntegerLiteral((integer) -> FeelExpressions.getAInteger(otherExpression)
                        .map(otherInteger -> Optional.of(comparison.apply(integer, otherInteger))).orElse(Optional.of(false)))
                .StringLiteral((__) -> Optional.of(false))
                .VariableLiteral((__) -> Optional.of(true))
                .RangeExpression((leftInc, lowerBound, upperBound, rightInc) ->
                        FeelExpressions.caseOf(otherExpression)
                                .RangeExpression((otherLeftInc, otherLowerBound, otherUpperBound, otherRightInc) ->
                                        subsumes(lowerBound, otherLowerBound, leftInc ? Comparison.LE : Comparison.LT).flatMap(
                                                subsumesLowerBound ->
                                                        subsumes(otherUpperBound, upperBound, rightInc ? Comparison.LE : Comparison.LT)
                                                                .flatMap(subsumesUpperBound -> Optional.of(subsumesLowerBound && subsumesUpperBound))))

                                .otherwise_(Optional.of(false))

                )
                .UnaryExpression((operator, operand) -> FeelExpressions.caseOf(otherExpression)
                        .RangeExpression((leftInc, lowerBound, upperBound, rightInc) -> {
                            switch (operator) {
                                case LT: return subsumes(upperBound, operand, rightInc ? Comparison.LT : Comparison.LE);
                                case GT: return subsumes(operand, lowerBound, leftInc ? Comparison.LT : Comparison.LE);
                                case LE: return subsumes(upperBound, operand, rightInc ? Comparison.LE : Comparison.LT);
                                case GE: return subsumes(operand, lowerBound, leftInc ? Comparison.LE : Comparison.LT);
                            }
                            return Optional.of(false);
                        })
                        .UnaryExpression((otherOperator, otherOperand) -> {
                            if (operator.equals(otherOperator) && operand.equals(otherOperand)) {
                                return Optional.of(true);
                            }
                            if (Comparison.fromOperator(operator).isGreater() && Comparison.fromOperator(otherOperator).isGreater()) {
                                return subsumes(otherOperand, operand, Comparison.fromOperator(operator));
                            } else if (Comparison.fromOperator(operator).isLess() && Comparison.fromOperator(otherOperator).isLess()) {
                                return subsumes(otherOperand, operand, Comparison.fromOperator(operator));
                            } else {
                                return Optional.of(false);
                            }
                        })
                        .otherwise_(Optional.of(false))
                )
                .otherwise_(Optional.empty());
    }

    enum Comparison {
        EQ {
            @Override
            public boolean apply(Boolean x, Boolean y) {
                return x == y;
            }

            @Override
            public boolean apply(Integer x, Integer y) {
                return Objects.equals(x, y);
            }

            @Override
            public boolean apply(Double x, Double y) {
                return Objects.equals(x, y);
            }

            @Override
            public boolean apply(LocalDateTime x, LocalDateTime y) {
                return x.isEqual(y);
            }
        },
        LT {
            @Override
            public boolean apply(Boolean x, Boolean y) {
                return false;
            }

            @Override
            public boolean apply(Integer x, Integer y) {
                return x < y;
            }

            @Override
            public boolean apply(Double x, Double y) {
                return x < y;
            }

            @Override
            public boolean apply(LocalDateTime x, LocalDateTime y) {
                return x.isBefore(y);
            }
        },
        LE {
            @Override
            public boolean apply(Boolean x, Boolean y) {
                return false;
            }

            @Override
            public boolean apply(Integer x, Integer y) {
                return x <= y;
            }

            @Override
            public boolean apply(Double x, Double y) {
                return x <= y;
            }

            @Override
            public boolean apply(LocalDateTime x, LocalDateTime y) {
                return x.isBefore(y) || x.isEqual(y);
            }
        }, GT {
            @Override
            public boolean apply(Boolean x, Boolean y) {
                return false;
            }

            @Override
            public boolean apply(Integer x, Integer y) {
                return x > y;
            }

            @Override
            public boolean apply(Double x, Double y) {
                return x > y;
            }

            @Override
            public boolean apply(LocalDateTime x, LocalDateTime y) {
                return x.isAfter(y);
            }
        },
        GE {
            @Override
            public boolean apply(Boolean x, Boolean y) {
                return false;
            }

            @Override
            public boolean apply(Integer x, Integer y) {
                return x >= y;
            }

            @Override
            public boolean apply(Double x, Double y) {
                return x >= y;
            }

            @Override
            public boolean apply(LocalDateTime x, LocalDateTime y) {
                return x.isAfter(y) || x.isEqual(y);
            }
        };

        public abstract boolean apply(Boolean x, Boolean y);
        public abstract boolean apply(Integer x, Integer y);
        public abstract boolean apply(Double x, Double y);
        public abstract boolean apply(LocalDateTime x, LocalDateTime y);

        public static Comparison fromOperator(final Operator operator) {
            switch (operator) {
                case LE: return Comparison.LE;
                case LT: return Comparison.LT;
                case GT: return Comparison.GT;
                case GE: return Comparison.GE;
                default: throw new IllegalArgumentException();
            }
        }

        public boolean isLess() {
            return LE.equals(this) || LT.equals(this);
        }

        public boolean isGreater() {
            return GE.equals(this) || GT.equals(this);
        }
    }
}
