package de.redsix.dmncheck.feel;

import static de.redsix.dmncheck.util.Eithers.left;
import static de.redsix.dmncheck.util.Eithers.right;

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

    public static Either<ValidationResult.Builder.ElementStep, ExpressionType> typecheck(
            final FeelExpression expression) {
        return typecheck(new Context(), expression);
    }

    public static Either<ValidationResult.Builder.ElementStep, ExpressionType> typecheck(
            final Context context, final FeelExpression expression) {
        return FeelExpressions.caseOf(expression)
                // FIXME: 12/10/17 The explicit type is needed as otherwise the type of 'right' is lost.
                .<Either<ValidationResult.Builder.ElementStep, ExpressionType>>Empty_(
                        right(de.redsix.dmncheck.feel.ExpressionTypes.TOP()))
                .Null_(right(ExpressionTypes.TOP()))
                .BooleanLiteral(bool -> right(ExpressionTypes.BOOLEAN()))
                .DateLiteral(dateTime -> right(ExpressionTypes.DATE()))
                .DoubleLiteral(aDouble -> right(ExpressionTypes.DOUBLE()))
                .IntegerLiteral(integer -> right(ExpressionTypes.INTEGER()))
                .StringLiteral(string -> right(ExpressionTypes.STRING()))
                .VariableLiteral(name -> {
                    if (context.containsKey(name)) {
                        return right(context.get(name));
                    } else {
                        return left(ValidationResult.init
                                .message("Variable '" + name + "' has no type.")
                                .severity(Severity.WARNING));
                    }
                })
                .RangeExpression(
                        (__, lowerBound, upperBound, ___) -> typecheckRangeExpression(context, lowerBound, upperBound))
                .UnaryExpression((operator, operand) -> typecheckUnaryExpression(context, operator, operand))
                .BinaryExpression((left, operator, right) -> typecheckBinaryExpression(context, left, operator, right))
                .DisjunctionExpression((head, tail) -> typecheckDisjunctionExpression(context, head, tail));
    }

    private static Either<ValidationResult.Builder.ElementStep, ExpressionType> typecheckDisjunctionExpression(
            final Context context, final FeelExpression head, final FeelExpression tail) {
        return typecheck(context, head).bind(headType -> typecheck(context, tail)
                .bind(tailType -> check(headType.equals(tailType), "Types of head and tail do not match.")
                        .orElse(right(headType))));
    }

    private static Either<ValidationResult.Builder.ElementStep, ExpressionType> typecheckBinaryExpression(
            final Context context, final FeelExpression left, final Operator operator, final FeelExpression right) {
        return typecheck(context, left).bind(leftType -> typecheck(context, right)
                .bind(rightType -> check(leftType.equals(rightType), "Types of left and right operand do not match.")
                        .orElse(checkOperatorCompatibility(leftType, operator))));
    }

    private static Either<ValidationResult.Builder.ElementStep, ExpressionType> typecheckUnaryExpression(
            final Context context, final Operator operator, final FeelExpression operand) {
        final Stream<Operator> allowedOperators =
                Stream.of(Operator.GT, Operator.GE, Operator.LT, Operator.LE, Operator.NOT, Operator.SUB);
        return typecheck(context, operand).bind(type -> check(
                        allowedOperators.anyMatch(operator::equals), "Operator is not supported in UnaryExpression.")
                .orElse(checkOperatorCompatibility(type, operator)));
    }

    private static Either<ValidationResult.Builder.ElementStep, ExpressionType> checkOperatorCompatibility(
            final ExpressionType type, final Operator operator) {
        return switch (operator) {
            case GE, GT, LE, LT, DIV, EXP, MUL, ADD, SUB -> check(
                            ExpressionType.isNumeric(type),
                            "Operator " + operator + " expects numeric type but got " + type)
                    .orElse(right(type));
            case OR, AND -> check(
                            ExpressionTypes.BOOLEAN().equals(type),
                            "Operator " + operator + " expects boolean but got " + type)
                    .orElse(right(type));
            case NOT -> right(type);
        };
    }

    private static Either<ValidationResult.Builder.ElementStep, ExpressionType> typecheckRangeExpression(
            final Context context, final FeelExpression lowerBound, final FeelExpression upperBound) {
        final List<ExpressionType> allowedTypes = Arrays.asList(
                ExpressionTypes.INTEGER(), ExpressionTypes.DOUBLE(), ExpressionTypes.LONG(), ExpressionTypes.DATE());
        return typecheck(context, lowerBound)
                .bind(lowerBoundType -> typecheck(context, upperBound).bind(upperBoundType -> check(
                                lowerBoundType.equals(upperBoundType), "Types of lower and upper bound do not match.")
                        .or(() -> check(
                                allowedTypes.contains(lowerBoundType), "Type is unsupported for RangeExpressions."))
                        .orElse(right(lowerBoundType))));
    }

    private static Optional<Either<ValidationResult.Builder.ElementStep, ExpressionType>> check(
            final Boolean condition, final String errorMessage) {
        if (!condition) {
            final ValidationResult.Builder.SeverityStep validationResult = ValidationResult.init.message(errorMessage);
            return Optional.of(left(validationResult));
        } else {
            return Optional.empty();
        }
    }
}
