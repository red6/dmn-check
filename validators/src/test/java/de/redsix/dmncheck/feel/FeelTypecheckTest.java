package de.redsix.dmncheck.feel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.util.Either;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FeelTypecheckTest {

    @Test
    void emptyHasTypeTop() {
        final FeelExpression expression = FeelParser.parser.parse("");
        final Either<
            ValidationResult.Builder.ElementStep,
            ExpressionType
        > type = FeelTypecheck.typecheck(expression);

        assertEquals(new Either.Right<>(new ExpressionType.Top()), type);
    }

    @ParameterizedTest
    @CsvSource({ "42", "3+4", "<3,>8", "[3..42]" })
    void hasTypeInteger(final String input) {
        final FeelExpression expression = FeelParser.parser.parse(input);
        final Either<
            ValidationResult.Builder.ElementStep,
            ExpressionType
        > type = FeelTypecheck.typecheck(expression);

        assertEquals(new Either.Right<>(new ExpressionType.Integer()), type);
    }

    @Test
    void doubleHasTypeDouble() {
        final FeelExpression expression = FeelParser.parser.parse(
            "3.14159265359"
        );
        final Either<
            ValidationResult.Builder.ElementStep,
            ExpressionType
        > type = FeelTypecheck.typecheck(expression);

        assertEquals(new Either.Right<>(new ExpressionType.Double()), type);
    }

    @Test
    void stringHasTypeString() {
        final FeelExpression expression = FeelParser.parser.parse("\"Steak\"");

        final Either<
            ValidationResult.Builder.ElementStep,
            ExpressionType
        > type = FeelTypecheck.typecheck(expression);

        assertEquals(new Either.Right<>(new ExpressionType.String()), type);
    }

    @ParameterizedTest
    @CsvSource({ "true, BOOLEAN", "false, BOOLEAN" })
    void trueHasTypeBoolean(String input, String expectedType) {
        final FeelExpression expression = FeelParser.parser.parse(input);

        final Either<
            ValidationResult.Builder.ElementStep,
            ExpressionType
        > type = FeelTypecheck.typecheck(expression);

        assertEquals(
            ExpressionTypeParser.parse(expectedType, Collections.emptyList()),
            type
        );
    }

    @Test
    void dateHasTypeDate() {
        final FeelExpression expression = FeelParser.parser.parse(
            "date and time(\"2015-11-30T12:00:00\")"
        );

        final Either<
            ValidationResult.Builder.ElementStep,
            ExpressionType
        > type = FeelTypecheck.typecheck(expression);

        assertEquals(new Either.Right<>(new ExpressionType.Date()), type);
    }

    @Test
    void boundVariableHasType() {
        final FeelExpression expression = FeelParser.parser.parse("x");
        final FeelTypecheck.Context context = new FeelTypecheck.Context();
        context.put("x", new ExpressionType.Integer());

        final Either<
            ValidationResult.Builder.ElementStep,
            ExpressionType
        > type = FeelTypecheck.typecheck(context, expression);

        assertEquals(new Either.Right<>(new ExpressionType.Integer()), type);
    }

    @Test
    void unboundVariableHasNoType() {
        final FeelExpression expression = FeelParser.parser.parse("x");
        final FeelTypecheck.Context context = new FeelTypecheck.Context();
        final Either<
            ValidationResult.Builder.ElementStep,
            ExpressionType
        > type = FeelTypecheck.typecheck(context, expression);

        assertEquals(Optional.empty(), type.getRight());
        assertEquals(
            "Variable 'x' has no type.",
            type.getLeft().orElseThrow(AssertionError::new).getMessage()
        );
    }

    @ParameterizedTest
    @CsvSource({ "<5, INTEGER", "<5.2, DOUBLE", "-1, INTEGER" })
    void lessThanExpressionHasNumericType(String input, String expectedType) {
        final FeelExpression expression = FeelParser.parser.parse(input);
        final Either<
            ValidationResult.Builder.ElementStep,
            ExpressionType
        > type = FeelTypecheck.typecheck(expression);

        assertEquals(
            ExpressionTypeParser.parse(expectedType, Collections.emptyList()),
            type
        );
    }

    @ParameterizedTest
    @CsvSource(
        {
            "<\"Steak\", Operator < expects numeric type but got STRING[]",
            "2+\"foo\", Types of left and right operand do not match.",
            "'<3,\"foo\"', Types of head and tail do not match.",
            "[1..1.5], Types of lower and upper bound do not match.",
            "[\"A\"..\"Z\"], Type is unsupported for RangeExpressions.",
        }
    )
    void isNotTypeable(final String input, final String errorMessage) {
        final FeelExpression expression = FeelParser.parser.parse(input);
        final Either<
            ValidationResult.Builder.ElementStep,
            ExpressionType
        > type = FeelTypecheck.typecheck(expression);

        assertEquals(Optional.empty(), type.getRight());
        assertEquals(
            errorMessage,
            type.getLeft().orElseThrow(AssertionError::new).getMessage()
        );
    }

    @Test
    void notExpressionContainingTrueIsTypable() {
        final FeelExpression expression = FeelParser.parser.parse("not(true)");
        final Either<
            ValidationResult.Builder.ElementStep,
            ExpressionType
        > type = FeelTypecheck.typecheck(expression);

        assertEquals(new Either.Right<>(new ExpressionType.Boolean()), type);
    }

    @Test
    void notExpressionContainingDisjunctionWithStringsIsTypable() {
        final FeelExpression expression = FeelParser.parser.parse(
            "not(\"foo\", \"bar\")"
        );
        final Either<
            ValidationResult.Builder.ElementStep,
            ExpressionType
        > type = FeelTypecheck.typecheck(expression);

        assertEquals(new Either.Right<>(new ExpressionType.String()), type);
    }
}
