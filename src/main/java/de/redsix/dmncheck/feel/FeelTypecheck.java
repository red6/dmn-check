package de.redsix.dmncheck.feel;

import de.redsix.dmncheck.model.ExpressionTypeEnum;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.util.Either;
import de.redsix.dmncheck.util.Eithers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import static de.redsix.dmncheck.util.Eithers.left;
import static de.redsix.dmncheck.util.Eithers.right;

public class FeelTypecheck {

    public final static class Context extends HashMap<String, ExpressionTypeEnum> { }

    public static Either<ExpressionTypeEnum, ValidationResult.Builder> typecheck(final FeelExpression expression) {
        return typecheck(new Context(), expression);
    }

    public static Either<ExpressionTypeEnum, ValidationResult.Builder> typecheck(final Context context, final FeelExpression expression) {
        return FeelExpressions.caseOf(expression)
                // FIXME: 12/10/17 The explicit type is needed as otherwise the type of 'right' is lost.
                .<Either<ExpressionTypeEnum, ValidationResult.Builder>>BooleanLiteral(bool -> left(ExpressionTypeEnum.BOOLEAN))
                .DateLiteral(dateTime -> left(ExpressionTypeEnum.DATE))
                .DoubleLiteral(aDouble -> left(ExpressionTypeEnum.DOUBLE))
                .IntegerLiteral(integer -> left(ExpressionTypeEnum.INTEGER))
                .StringLiteral(string -> left(ExpressionTypeEnum.STRING))
                .VariableLiteral(name -> {
                    if (context.containsKey(name)) {
                        return left(context.get(name));
                    } else {
                        return right(ValidationResult.Builder.with($ ->
                                $.message = "Variable '" + name + "' has no type."));
                    }
                })
                .RangeExpression((__, lowerBound, upperBound, ___) -> typecheckRangeExpression(context, lowerBound, upperBound))
                .UnaryExpression((operator, operand) -> typecheckUnaryExpression(context, operator, operand))
                .BinaryExpression((left, operator, right) -> typecheckBinaryExpression(context, left, operator, right))
                .DisjunctionExpression((head, tail) -> typecheckDisjunctionExpression(context, head, tail)
                );
    }

    private static Either<ExpressionTypeEnum, ValidationResult.Builder> typecheckDisjunctionExpression(final Context context, final FeelExpression head, final FeelExpression tail) {
        return typecheck(context, head).bind(headType ->
                typecheck(context, tail).bind(tailType -> {
                    if (headType.equals(tailType)) {
                        return Eithers.left(headType);
                    } else {
                        return Eithers.right(ValidationResult.Builder.with($ ->
                                $.message = "Types of head and tail do not match."));
                    }
                }));
    }

    private static Either<ExpressionTypeEnum, ValidationResult.Builder> typecheckBinaryExpression(final Context context, final FeelExpression left, final Operator operator, final FeelExpression right) {
        return typecheck(context, left).bind(leftType ->
                typecheck(context, right).bind(rightType -> {
                    if (leftType.equals(rightType)) {
                        return Eithers.left(leftType);
                    } else {
                        return Eithers.right(
                                ValidationResult.Builder.with($ ->
                                        $.message = "Types of left and right operand do not match."));
                    }
                }));
    }

    private static Either<ExpressionTypeEnum, ValidationResult.Builder> typecheckUnaryExpression(final Context context, final Operator operator, final FeelExpression operand) {
        return typecheck(context, operand).bind(type -> {
            if (Stream.of(Operator.GT, Operator.GE, Operator.LT, Operator.LE).anyMatch(
                    operator::equals) &&
                    ExpressionTypeEnum.isNumeric(type)) {
                return Eithers.left(type);
            } else {
                return Eithers.right(ValidationResult.Builder.with($ ->
                        $.message = "Expression has wrong type."));
            }
        });
    }

    private static Either<ExpressionTypeEnum, ValidationResult.Builder> typecheckRangeExpression(final Context context, final FeelExpression lowerBound, final FeelExpression upperBound) {
        final List<ExpressionTypeEnum> allowedTypes = Arrays
                .asList(ExpressionTypeEnum.INTEGER, ExpressionTypeEnum.DOUBLE, ExpressionTypeEnum.LONG, ExpressionTypeEnum.DATE);
        return typecheck(context, lowerBound).bind(lowerBoundType ->
                typecheck(context, upperBound).bind(upperBoundType -> {
                    if (lowerBoundType.equals(upperBoundType) && allowedTypes.contains(lowerBoundType)) {
                        return Eithers.left(lowerBoundType);
                    } else {
                        return Eithers.right(ValidationResult.Builder
                                .with($ -> $.message = "Types of lower and upper bound do not match or are unsupported for RangeExpressions."));
                    }
        }));
    }
}
