package de.redsix.dmncheck.feel;

import de.redsix.dmncheck.feel.ExpressionType.BOOLEAN;
import de.redsix.dmncheck.feel.ExpressionType.DATE;
import de.redsix.dmncheck.feel.ExpressionType.DOUBLE;
import de.redsix.dmncheck.feel.ExpressionType.INTEGER;
import de.redsix.dmncheck.feel.ExpressionType.LONG;
import de.redsix.dmncheck.feel.ExpressionType.STRING;
import de.redsix.dmncheck.feel.ExpressionType.TOP;
import de.redsix.dmncheck.feel.FeelExpression.BooleanLiteral;
import de.redsix.dmncheck.feel.FeelExpression.DateLiteral;
import de.redsix.dmncheck.feel.FeelExpression.DateTimeLiteral;
import de.redsix.dmncheck.feel.FeelExpression.DisjunctionExpression;
import de.redsix.dmncheck.feel.FeelExpression.DoubleLiteral;
import de.redsix.dmncheck.feel.FeelExpression.Empty;
import de.redsix.dmncheck.feel.FeelExpression.IntegerLiteral;
import de.redsix.dmncheck.feel.FeelExpression.NaryExpression;
import de.redsix.dmncheck.feel.FeelExpression.Null;
import de.redsix.dmncheck.feel.FeelExpression.RangeExpression;
import de.redsix.dmncheck.feel.FeelExpression.StringLiteral;
import de.redsix.dmncheck.feel.FeelExpression.VariableLiteral;
import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.result.ValidationResult.Builder.ElementStep;
import de.redsix.dmncheck.result.ValidationResult.Builder.SeverityStep;
import de.redsix.dmncheck.util.Either;

import de.redsix.dmncheck.util.Either.Left;
import de.redsix.dmncheck.util.Either.Right;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class FeelTypecheck {

    private FeelTypecheck() {
    }

    public static final class Context extends HashMap<String, ExpressionType> {
    }

    public static Either<ElementStep, ExpressionType> typecheck(
            final FeelExpression expression) {
        return typecheck(new Context(), expression);
    }

    public static Either<ElementStep, ExpressionType> typecheck(
            final Context context, final FeelExpression expression) {
        return switch (expression) {
            case Empty() -> new Right<>(new TOP());
            case Null() -> new Right<>(new TOP());
            case BooleanLiteral(var bool) -> new Right<>(new BOOLEAN());
            case DateLiteral(var date) -> new Right<>(new DATE());
            case DateTimeLiteral(var dateTime) -> new Right<>(new DATE());
            case DoubleLiteral(var aDouble) -> new Right<>(new DOUBLE());
            case IntegerLiteral(var integer) -> new Right<>(new INTEGER());
            case StringLiteral(var string) -> new Right<>(new STRING());
            case VariableLiteral(var name) -> {
                if (context.containsKey(name)) {
                    yield new Right<>(context.get(name));
                } else {
                    yield new Left<>(
                            ValidationResult.init
                                    .message("Variable '" + name + "' has no type.")
                                    .severity(Severity.WARNING)
                    );
                }
            }
            case RangeExpression(var __, var lowerBound, var upperBound, var ___) ->
                    typecheckRangeExpression(context, lowerBound, upperBound);
            case NaryExpression(var operator, var operands) -> typecheckNaryExpression(context, operator, operands);
            case DisjunctionExpression(var head, var tail) -> typecheckDisjunctionExpression(context, head, tail);
        };
    }

    private static Either<ElementStep, ExpressionType> typecheckDisjunctionExpression(
            final Context context, final FeelExpression head, final FeelExpression tail) {
        return typecheck(context, head).bind(headType -> typecheck(context, tail)
                .bind(tailType -> check(headType.equals(tailType), "Types of head and tail do not match.")
                        .orElse(new Right<>(headType))));
    }

    private static Either<ElementStep, ExpressionType> typecheckNaryExpression(
            final Context context, final Operator operator, final List<FeelExpression> operands) {
        //final Stream<Operator> allowedOperators =
        //        Stream.of(Operator.GT, Operator.GE, Operator.LT, Operator.LE, Operator.NOT, Operator.SUB);
        return operands.stream().map(operand -> typecheck(context, operand)).collect(Either.reduce())
                .bind(types -> checkOperatorCompatibility(types, operator));
    }

    private static Either<ElementStep, ExpressionType> checkOperatorCompatibility(
            final List<ExpressionType> types, final Operator operator) {
        return switch (operator) {
            case GE, GT, LE, LT, DIV, EXP, MUL, ADD, SUB -> check(
                    types.stream().allMatch(ExpressionType::isNumeric),
                    "Operator " + operator + " expects numeric type but got " + types)
                    .orElse(new Right<>(types.getFirst()));
            case OR, AND -> check(
                    types.stream().allMatch(type -> new BOOLEAN().equals(type)),
                    "Operator " + operator + " expects boolean but got " + types)
                    .orElse(new Right<>(types.getFirst()));
            case NOT, DATE, DATE_AND_TIME ->  check(
                types.size() == 1,
                "Operator " + operator + " exactly one argument, but got " + types)
                .orElse(new Right<>(types.getFirst()));
        };
    }

    private static Either<ElementStep, ExpressionType> typecheckRangeExpression(
            final Context context, final FeelExpression lowerBound, final FeelExpression upperBound) {
        final List<ExpressionType> allowedTypes = Arrays.asList(
                new INTEGER(), new DOUBLE(), new LONG(), new DATE());
        return typecheck(context, lowerBound)
                .bind(lowerBoundType -> typecheck(context, upperBound).bind(upperBoundType -> check(
                        lowerBoundType.equals(upperBoundType), "Types of lower and upper bound do not match.")
                        .or(() -> check(
                                allowedTypes.contains(lowerBoundType), "Type is unsupported for RangeExpressions."))
                        .orElse(new Right<>(lowerBoundType))));
    }

    private static Optional<Either<ElementStep, ExpressionType>> check(
            final Boolean condition, final String errorMessage) {
        if (!condition) {
            final SeverityStep validationResult = ValidationResult.init.message(errorMessage);
            return Optional.of(new Left<>(validationResult));
        } else {
            return Optional.empty();
        }
    }
}
