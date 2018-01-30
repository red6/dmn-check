package de.redsix.dmncheck.feel;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

final class Subsumption {

    static Optional<Boolean> subsumes(final FeelExpression expression, final FeelExpression otherExpression, final Comparison comparison) {
        return FeelExpressions.caseOf(expression)
                .Empty(() -> Optional.of(true))
                .BooleanLiteral((aBool) -> Optional.of(subsumesBooleanLiteral(aBool, otherExpression, comparison)))
                .DateLiteral((dateTime) -> Optional.of(subsumesDateTime(dateTime, otherExpression, comparison)))
                .DoubleLiteral((aDouble) -> Optional.of(subsumesDouble(aDouble, otherExpression, comparison)))
                .IntegerLiteral((integer) -> Optional.of(subsumesInteger(integer, otherExpression, comparison)))
                .StringLiteral((__) -> Optional.of(false))
                .VariableLiteral((__) -> Optional.of(true))
                .RangeExpression((leftInc, lowerBound, upperBound, rightInc) ->
                        subsumesRangeExpression(leftInc, lowerBound, upperBound, rightInc, otherExpression))
                .UnaryExpression((operator, operand) -> subsumesUnaryExpression(operator, operand, otherExpression))
                .otherwise_(Optional.empty());
    }

    private static Optional<Boolean> subsumesUnaryExpression(Operator operator, FeelExpression operand, FeelExpression otherExpression) {
        return FeelExpressions.caseOf(otherExpression)
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
                .otherwise_(Optional.of(false));
    }

    private static Optional<Boolean> subsumesRangeExpression(boolean leftInc, FeelExpression lowerBound, FeelExpression upperBound,
            boolean rightInc, FeelExpression otherExpression) {
        return FeelExpressions.caseOf(otherExpression)
                .RangeExpression((otherLeftInc, otherLowerBound, otherUpperBound, otherRightInc) ->
                        subsumes(lowerBound, otherLowerBound, leftInc ? Comparison.LE : Comparison.LT).flatMap(
                                subsumesLowerBound ->
                                        subsumes(otherUpperBound, upperBound, rightInc ? Comparison.LE : Comparison.LT).flatMap(
                                                subsumesUpperBound ->
                                                        Optional.of(subsumesLowerBound && subsumesUpperBound))))

                .otherwise_(Optional.of(false));
    }

    private static Boolean subsumesInteger(Integer integer, FeelExpression otherExpression, Comparison comparison) {
        return FeelExpressions.getAInteger(otherExpression)
                .map(otherInteger -> comparison.apply(integer, otherInteger))
                .orElse(false);
    }

    private static Boolean subsumesDouble(Double aDouble, FeelExpression otherExpression, Comparison comparison) {
        return FeelExpressions.getADouble(otherExpression)
                .map(otherDouble -> comparison.apply(aDouble, otherDouble))
                .orElse(false);
    }

    private static Boolean subsumesDateTime(LocalDateTime dateTime, FeelExpression otherExpression, Comparison comparison) {
        return FeelExpressions.getDateTime(otherExpression)
                .map(otherDateTime -> comparison.apply(dateTime, otherDateTime))
                .orElse(false);
    }

    private static Boolean subsumesBooleanLiteral(Boolean aBool, FeelExpression otherExpression, Comparison comparison) {
        return FeelExpressions.getABoolean(otherExpression)
                .map(otherBool -> comparison.apply(aBool, otherBool))
                .orElse(false);
    }

    enum Comparison {
        EQ {
            @Override
            public boolean apply(Boolean x, Boolean y) {
                return Objects.equals(x, y);
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
