package de.redsix.dmncheck.feel;

import de.redsix.dmncheck.model.ExpressionTypeEnum;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.util.Either;
import de.redsix.dmncheck.util.Eithers;

import java.util.HashMap;
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
                .<Either<ExpressionTypeEnum, ValidationResult.Builder>>
                        BooleanLiteral(bool -> left(ExpressionTypeEnum.BOOLEAN))
                .DateLiteral(dateTime -> left(ExpressionTypeEnum.DATE))
                .DoubleLiteral(aDouble -> left(ExpressionTypeEnum.DOUBLE))
                .IntegerLiteral(integer -> left(ExpressionTypeEnum.INTEGER))
                .StringLiteral(string -> left(ExpressionTypeEnum.STRING))
                .VariableLiteral(name -> {
                    if (context.containsKey(name)) {
                        return left(context.get(name));
                    } else {
                        return right(
                                ValidationResult.Builder.with($ -> $.message = "Variable '" + name + "' has no type."));
                    }
                })
                .RangeExpression((x, lowerBound, upperBound, y) ->
                        typecheck(context, lowerBound).bind(lowerBoundType ->
                                typecheck(context, upperBound).bind(upperBoundType -> {
                                    if (lowerBoundType.equals(upperBoundType)) {
                                        return Eithers.<ExpressionTypeEnum, ValidationResult.Builder>left(
                                                lowerBoundType);
                                    } else {
                                        return Eithers.<ExpressionTypeEnum, ValidationResult.Builder>right(
                                                ValidationResult.Builder.with(
                                                        $ -> $.message = "Types of lower and upper bound do not match."));
                                    }
                                }))
                )
                .UnaryExpression((operator, operand) ->
                        typecheck(context, operand).bind(type -> {
                            if (Stream.of(Operator.GT, Operator.GE, Operator.LT, Operator.LE).anyMatch(
                                    operator::equals) &&
                                    ExpressionTypeEnum.isNumeric(type)) {
                                return Eithers.<ExpressionTypeEnum, ValidationResult.Builder>left(type);
                            } else {
                                return Eithers.<ExpressionTypeEnum, ValidationResult.Builder>right(
                                        ValidationResult.Builder.with($ -> $.message = "Expression has wrong type."));
                            }
                        })
                )
                .BinaryExpression((left, operator, right) ->
                        typecheck(context, left).bind(leftType ->
                                typecheck(context, right).bind(rightType -> {
                                    if (leftType.equals(rightType)) {
                                        return Eithers.<ExpressionTypeEnum, ValidationResult.Builder>left(
                                                leftType);
                                    } else {
                                        return Eithers.<ExpressionTypeEnum, ValidationResult.Builder>right(
                                                ValidationResult.Builder.with(
                                                        $ -> $.message = "Types of left and right operand do not match."));
                                    }
                                }))
                )
                .DisjunctionExpression((head, tail) ->
                        typecheck(context, head).bind(headType ->
                                typecheck(context, tail).bind(tailType -> {
                                    if (headType.equals(tailType)) {
                                        return Eithers.<ExpressionTypeEnum, ValidationResult.Builder>left(
                                                headType);
                                    } else {
                                        return Eithers.<ExpressionTypeEnum, ValidationResult.Builder>right(
                                                ValidationResult.Builder.with(
                                                        $ -> $.message = "Types of head and tail do not match."));
                                    }
                                }))
                );
    }
}
