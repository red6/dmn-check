package de.redsix.dmncheck.feel;

import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.util.Either;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class FeelTypecheck {

    private FeelTypecheck() {}

    public static final class Context extends HashMap<String, ExpressionType> {}

    public static Either<
        ValidationResult.Builder.ElementStep,
        ExpressionType
    > typecheck(final FeelExpression expression) {
        return typecheck(new Context(), expression);
    }

    public static Either<
        ValidationResult.Builder.ElementStep,
        ExpressionType
    > typecheck(final Context context, final FeelExpression expression) {
        return switch (expression) {
            case FeelExpression.Empty() -> new Either.Right<>(
                new ExpressionType.TOP()
            );
            case FeelExpression.Null() -> new Either.Right<>(
                new ExpressionType.TOP()
            );
            case FeelExpression.BooleanLiteral(var bool) -> new Either.Right<>(
                new ExpressionType.BOOLEAN()
            );
            case FeelExpression.DateLiteral(var dateTime) -> new Either.Right<>(
                new ExpressionType.DATE()
            );
            case FeelExpression.DoubleLiteral(
                var aDouble
            ) -> new Either.Right<>(new ExpressionType.DOUBLE());
            case FeelExpression.IntegerLiteral(
                var integer
            ) -> new Either.Right<>(new ExpressionType.INTEGER());
            case FeelExpression.StringLiteral(var string) -> new Either.Right<>(
                new ExpressionType.STRING()
            );
            case FeelExpression.VariableLiteral(var name) -> {
                if (context.containsKey(name)) {
                    yield new Either.Right<>(context.get(name));
                } else {
                    yield new Either.Left<>(
                        ValidationResult.init
                            .message("Variable '" + name + "' has no type.")
                            .severity(Severity.WARNING)
                    );
                }
            }
            case FeelExpression.RangeExpression(
                var __,
                var lowerBound,
                var upperBound,
                var ___
            ) -> typecheckRangeExpression(context, lowerBound, upperBound);
            case FeelExpression.UnaryExpression(
                var operator,
                var operand
            ) -> typecheckUnaryExpression(context, operator, operand);
            case FeelExpression.BinaryExpression(
                var left,
                var operator,
                var right
            ) -> typecheckBinaryExpression(context, left, operator, right);
            case FeelExpression.DisjunctionExpression(
                var head,
                var tail
            ) -> typecheckDisjunctionExpression(context, head, tail);
        };
    }

    private static Either<
        ValidationResult.Builder.ElementStep,
        ExpressionType
    > typecheckDisjunctionExpression(
        final Context context,
        final FeelExpression head,
        final FeelExpression tail
    ) {
        return typecheck(context, head).bind(headType ->
            typecheck(context, tail).bind(tailType ->
                check(
                    headType.equals(tailType),
                    "Types of head and tail do not match."
                ).orElse(new Either.Right<>(headType))
            )
        );
    }

    private static Either<
        ValidationResult.Builder.ElementStep,
        ExpressionType
    > typecheckBinaryExpression(
        final Context context,
        final FeelExpression left,
        final Operator operator,
        final FeelExpression right
    ) {
        return typecheck(context, left).bind(leftType ->
            typecheck(context, right).bind(rightType ->
                check(
                    leftType.equals(rightType),
                    "Types of left and right operand do not match."
                ).orElse(checkOperatorCompatibility(leftType, operator))
            )
        );
    }

    private static Either<
        ValidationResult.Builder.ElementStep,
        ExpressionType
    > typecheckUnaryExpression(
        final Context context,
        final Operator operator,
        final FeelExpression operand
    ) {
        final Stream<Operator> allowedOperators = Stream.of(
            Operator.GT,
            Operator.GE,
            Operator.LT,
            Operator.LE,
            Operator.NOT,
            Operator.SUB
        );
        return typecheck(context, operand).bind(type ->
            check(
                allowedOperators.anyMatch(operator::equals),
                "Operator is not supported in UnaryExpression."
            ).orElse(checkOperatorCompatibility(type, operator))
        );
    }

    private static Either<
        ValidationResult.Builder.ElementStep,
        ExpressionType
    > checkOperatorCompatibility(
        final ExpressionType type,
        final Operator operator
    ) {
        return switch (operator) {
            case GE, GT, LE, LT, DIV, EXP, MUL, ADD, SUB -> check(
                ExpressionType.isNumeric(type),
                "Operator " + operator + " expects numeric type but got " + type
            ).orElse(new Either.Right<>(type));
            case OR, AND -> check(
                new ExpressionType.BOOLEAN().equals(type),
                "Operator " + operator + " expects boolean but got " + type
            ).orElse(new Either.Right<>(type));
            case NOT -> new Either.Right<>(type);
        };
    }

    private static Either<
        ValidationResult.Builder.ElementStep,
        ExpressionType
    > typecheckRangeExpression(
        final Context context,
        final FeelExpression lowerBound,
        final FeelExpression upperBound
    ) {
        final List<ExpressionType> allowedTypes = Arrays.asList(
            new ExpressionType.INTEGER(),
            new ExpressionType.DOUBLE(),
            new ExpressionType.LONG(),
            new ExpressionType.DATE()
        );
        return typecheck(context, lowerBound).bind(lowerBoundType ->
                typecheck(context, upperBound).bind(upperBoundType ->
                        check(
                            lowerBoundType.equals(upperBoundType),
                            "Types of lower and upper bound do not match."
                        )
                            .or(() ->
                                check(
                                    allowedTypes.contains(lowerBoundType),
                                    "Type is unsupported for RangeExpressions."
                                )
                            )
                            .orElse(new Either.Right<>(lowerBoundType))
                    )
            );
    }

    private static Optional<
        Either<ValidationResult.Builder.ElementStep, ExpressionType>
    > check(final Boolean condition, final String errorMessage) {
        if (!condition) {
            final ValidationResult.Builder.SeverityStep validationResult =
                ValidationResult.init.message(errorMessage);
            return Optional.of(new Either.Left<>(validationResult));
        } else {
            return Optional.empty();
        }
    }
}
