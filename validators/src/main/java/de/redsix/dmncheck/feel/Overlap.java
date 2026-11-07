package de.redsix.dmncheck.feel;

import java.util.Optional;
import org.checkerframework.checker.nullness.qual.Nullable;

final class Overlap {

    /** A set of values between two bounds; a {@code null} bound means unbounded. */
    private record Interval(
            @Nullable Comparable<?> lower, boolean isLowerInclusive, @Nullable Comparable<?> upper, boolean isUpperInclusive) {

        static Interval point(final Comparable<?> value) {
            return new Interval(value, true, value, true);
        }

        boolean isPoint(final Comparable<?> value) {
            return isLowerInclusive
                    && isUpperInclusive
                    && lower != null
                    && upper != null
                    && compare(lower, value) == 0
                    && compare(upper, value) == 0;
        }
    }

    private Overlap() {}

    /**
     * Checks whether there is a value that matches both expressions.
     *
     * @return {@code Optional.empty()} if this cannot be decided
     */
    static Optional<Boolean> overlaps(final FeelExpression expression, final FeelExpression otherExpression) {
        return switch (expression) {
            case FeelExpression.Empty() -> Optional.of(true);
            case FeelExpression.DisjunctionExpression(final var head, final var tail) ->
                    or(overlaps(head, otherExpression), overlaps(tail, otherExpression));
            default -> switch (otherExpression) {
                case FeelExpression.Empty() -> Optional.of(true);
                case FeelExpression.DisjunctionExpression(final var head, final var tail) ->
                        or(overlaps(expression, head), overlaps(expression, tail));
                default -> overlapsSimpleExpressions(expression, otherExpression);
            };
        };
    }

    private static Optional<Boolean> overlapsSimpleExpressions(
            final FeelExpression expression, final FeelExpression otherExpression) {
        if (expression instanceof FeelExpression.Null() || otherExpression instanceof FeelExpression.Null()) {
            if (expression instanceof FeelExpression.Null() && otherExpression instanceof FeelExpression.Null()) {
                return Optional.of(true);
            }
            return toInterval(expression).or(() -> toInterval(otherExpression)).map(__ -> false);
        }

        final Optional<Boolean> negated = overlapsNegation(expression, otherExpression);
        if (negated.isPresent()) {
            return negated;
        }
        final Optional<Boolean> otherNegated = overlapsNegation(otherExpression, expression);
        if (otherNegated.isPresent()) {
            return otherNegated;
        }

        return toInterval(expression)
                .flatMap(interval -> toInterval(otherExpression).flatMap(other -> intersects(interval, other)));
    }

    /** {@code not(x)} matches everything except {@code x}, so it overlaps every set except exactly {@code x}. */
    private static Optional<Boolean> overlapsNegation(
            final FeelExpression expression, final FeelExpression otherExpression) {
        if (expression instanceof FeelExpression.NaryExpression(final var operator, final var operands)
                && operator == Operator.NOT
                && operands.size() == 1) {
            return toValue(operands.getFirst())
                    .flatMap(value -> toInterval(otherExpression)
                            .filter(other -> sameType(value, other))
                            .map(other -> !other.isPoint(value)));
        }
        return Optional.empty();
    }

    private static Optional<Interval> toInterval(final FeelExpression expression) {
        if (expression.isDate()) {
            return toInterval(expression.extractDateExpression());
        }
        return switch (expression) {
            case FeelExpression.RangeExpression(
                    final var isLeftInclusive,
                    final var lowerBound,
                    final var upperBound,
                    final var isRightInclusive) ->
                    toValue(lowerBound).flatMap(lower -> toValue(upperBound)
                            .filter(upper -> lower.getClass().equals(upper.getClass()))
                            .map(upper -> new Interval(lower, isLeftInclusive, upper, isRightInclusive)));
            case FeelExpression.NaryExpression(final var operator, final var operands) when operands.size() == 1 ->
                    toValue(operands.getFirst()).flatMap(value -> switch (operator) {
                        case LT -> Optional.of(new Interval(null, false, value, false));
                        case LE -> Optional.of(new Interval(null, false, value, true));
                        case GT -> Optional.of(new Interval(value, false, null, false));
                        case GE -> Optional.of(new Interval(value, true, null, false));
                        default -> Optional.empty();
                    });
            default -> toValue(expression).map(Interval::point);
        };
    }

    private static Optional<Comparable<?>> toValue(final FeelExpression expression) {
        if (expression.isDate()) {
            return toValue(expression.extractDateExpression());
        }
        return switch (expression) {
            case FeelExpression.BooleanLiteral(final var aBoolean) -> Optional.of(aBoolean);
            // Integers and doubles are both FEEL numbers and have to be comparable with each other
            case FeelExpression.IntegerLiteral(final var anInteger) -> Optional.of(anInteger.doubleValue());
            case FeelExpression.DoubleLiteral(final var aDouble) -> Optional.of(aDouble);
            case FeelExpression.StringLiteral(final var aString) -> Optional.of(aString);
            case FeelExpression.DateLiteral(final var aDate) -> Optional.of(aDate);
            case FeelExpression.DateTimeLiteral(final var aDateTime) -> Optional.of(aDateTime);
            default -> Optional.empty();
        };
    }

    private static Optional<Boolean> intersects(final Interval interval, final Interval other) {
        if (!sameType(interval, other)) {
            return Optional.empty();
        }

        // The intersection is bounded by the higher of both lower bounds and the lower of both upper bounds
        final @Nullable Comparable<?> lower = interval.lower();
        final @Nullable Comparable<?> otherLower = other.lower();
        final int lowerComparison = lower == null ? -1 : otherLower == null ? 1 : compare(lower, otherLower);
        final @Nullable Comparable<?> intersectionLower = lowerComparison > 0 ? lower : otherLower;
        final boolean isLowerInclusive = lowerComparison == 0
                ? interval.isLowerInclusive() && other.isLowerInclusive()
                : lowerComparison > 0 ? interval.isLowerInclusive() : other.isLowerInclusive();

        final @Nullable Comparable<?> upper = interval.upper();
        final @Nullable Comparable<?> otherUpper = other.upper();
        final int upperComparison = upper == null ? 1 : otherUpper == null ? -1 : compare(upper, otherUpper);
        final @Nullable Comparable<?> intersectionUpper = upperComparison < 0 ? upper : otherUpper;
        final boolean isUpperInclusive = upperComparison == 0
                ? interval.isUpperInclusive() && other.isUpperInclusive()
                : upperComparison < 0 ? interval.isUpperInclusive() : other.isUpperInclusive();

        if (intersectionLower == null || intersectionUpper == null) {
            return Optional.of(true);
        }
        final int comparison = compare(intersectionLower, intersectionUpper);
        return Optional.of(comparison < 0 || comparison == 0 && isLowerInclusive && isUpperInclusive);
    }

    private static boolean sameType(final Comparable<?> value, final Interval interval) {
        return sameType(Interval.point(value), interval);
    }

    private static boolean sameType(final Interval interval, final Interval other) {
        final @Nullable Class<?> type = typeOf(interval);
        final @Nullable Class<?> otherType = typeOf(other);
        return type != null && type.equals(otherType);
    }

    private static @Nullable Class<?> typeOf(final Interval interval) {
        final @Nullable Comparable<?> bound = interval.lower() != null ? interval.lower() : interval.upper();
        return bound != null ? bound.getClass() : null;
    }

    @SuppressWarnings("unchecked")
    private static int compare(final Comparable<?> value, final Comparable<?> otherValue) {
        return ((Comparable<Object>) value).compareTo(otherValue);
    }

    private static Optional<Boolean> or(final Optional<Boolean> result, final Optional<Boolean> otherResult) {
        if (result.orElse(false) || otherResult.orElse(false)) {
            return Optional.of(true);
        }
        return result.isPresent() && otherResult.isPresent() ? Optional.of(false) : Optional.empty();
    }
}
