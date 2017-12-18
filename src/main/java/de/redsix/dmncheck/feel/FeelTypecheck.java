package de.redsix.dmncheck.feel;

import de.redsix.dmncheck.model.ExpressionType;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.util.Either;
import de.redsix.dmncheck.util.Eithers;

import javax.enterprise.inject.spi.Producer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static de.redsix.dmncheck.util.Eithers.left;
import static de.redsix.dmncheck.util.Eithers.right;

public class FeelTypecheck {

    public final static class Context extends HashMap<String, ExpressionType> { }

    public static Either<ExpressionType, ValidationResult.Builder> typecheck(final FeelExpression expression) {
        return typecheck(new Context(), expression);
    }

    public static Either<ExpressionType, ValidationResult.Builder> typecheck(final Context context, final FeelExpression expression) {
        return FeelExpressions.caseOf(expression)
                // FIXME: 12/10/17 The explicit type is needed as otherwise the type of 'right' is lost.
                .<Either<ExpressionType, ValidationResult.Builder>>BooleanLiteral(bool -> left(ExpressionType.BOOLEAN))
                .DateLiteral(dateTime -> left(ExpressionType.DATE))
                .DoubleLiteral(aDouble -> left(ExpressionType.DOUBLE))
                .IntegerLiteral(integer -> left(ExpressionType.INTEGER))
                .StringLiteral(string -> left(ExpressionType.STRING))
                .VariableLiteral(name ->
                    check(context.containsKey(name), "Variable '" + name + "' has no type.")
                    .orElse(left(context.get(name))))
                .RangeExpression((__, lowerBound, upperBound, ___) -> typecheckRangeExpression(context, lowerBound, upperBound))
                .UnaryExpression((operator, operand) -> typecheckUnaryExpression(context, operator, operand))
                .BinaryExpression((left, operator, right) -> typecheckBinaryExpression(context, left, operator, right))
                .DisjunctionExpression((head, tail) -> typecheckDisjunctionExpression(context, head, tail)
                );
    }

    private static Either<ExpressionType, ValidationResult.Builder> typecheckDisjunctionExpression(final Context context, final FeelExpression head, final FeelExpression tail) {
        return typecheck(context, head).bind(headType ->
                typecheck(context, tail).bind(tailType ->
                    check(headType.equals(tailType), "Types of head and tail do not match.")
                            .orElse(left(headType))
                ));
    }

    private static Either<ExpressionType, ValidationResult.Builder> typecheckBinaryExpression(final Context context, final FeelExpression left, final Operator operator, final FeelExpression right) {
        return typecheck(context, left).bind(leftType ->
                typecheck(context, right).bind(rightType ->
                    check(leftType.equals(rightType), "Types of left and right operand do not match.")
                    .orElse(left(leftType))
                ));
    }

    private static Either<ExpressionType, ValidationResult.Builder> typecheckUnaryExpression(final Context context, final Operator operator, final FeelExpression operand) {
        final Stream<Operator> allowedOperators = Stream.of(Operator.GT, Operator.GE, Operator.LT, Operator.LE);
        return typecheck(context, operand).bind(type ->
                    check(allowedOperators.anyMatch(operator::equals), "Operator is not supported in UnaryExpression.").map(Optional::of)
                    .orElseGet(() -> check(ExpressionType.isNumeric(type), "Non-numeric type in UnaryExpression."))
                    .orElse(left(type))
                );
    }

    private static Either<ExpressionType, ValidationResult.Builder> typecheckRangeExpression(final Context context, final FeelExpression lowerBound, final FeelExpression upperBound) {
        final List<ExpressionType> allowedTypes = Arrays
                .asList(ExpressionType.INTEGER, ExpressionType.DOUBLE, ExpressionType.LONG, ExpressionType.DATE);
        return typecheck(context, lowerBound).bind(lowerBoundType ->
                typecheck(context, upperBound).bind(upperBoundType ->
                        check(lowerBoundType.equals(upperBoundType), "Types of lower and upper bound do not match.").map(Optional::of)
                        .orElseGet(() -> check(allowedTypes.contains(lowerBoundType), "Type is unsupported for RangeExpressions."))
                        .orElse(left(lowerBoundType))
                ));
    }

    private static Optional<Either<ExpressionType, ValidationResult.Builder>> check(final Boolean condition, final String errorMessage) {
        if (!condition) {
            return Optional.of(Eithers.right(ValidationResult.Builder.with($ -> $.message = errorMessage)));
        } else {
            return Optional.empty();
        }
    }
}
