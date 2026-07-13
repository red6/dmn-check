package de.redsix.dmncheck.feel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.redsix.dmncheck.feel.ExpressionType.DATE;
import de.redsix.dmncheck.feel.ExpressionType.INTEGER;
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
        final FeelExpression expression = FeelParser.PARSER.parse("");
        final Either<ValidationResult.Builder.ElementStep, ExpressionType> type = FeelTypecheck.typecheck(expression);

        assertEquals(new Either.Right<>(new ExpressionType.TOP()), type);
    }

    @ParameterizedTest
    @CsvSource({"42", "3+4", "<3,>8", "[3..42]"})
    void hasTypeInteger(final String input) {
        final FeelExpression expression = FeelParser.PARSER.parse(input);
        final Either<ValidationResult.Builder.ElementStep, ExpressionType> type = FeelTypecheck.typecheck(expression);

        assertEquals(new Either.Right<>(new ExpressionType.INTEGER()), type);
    }

    @Test
    void doubleHasTypeDouble() {
        final FeelExpression expression = FeelParser.PARSER.parse("3.14159265359");
        final Either<ValidationResult.Builder.ElementStep, ExpressionType> type = FeelTypecheck.typecheck(expression);

        assertEquals(new Either.Right<>(new ExpressionType.DOUBLE()), type);
    }

    @Test
    void stringHasTypeString() {
        final FeelExpression expression = FeelParser.PARSER.parse("\"Steak\"");

        final Either<ValidationResult.Builder.ElementStep, ExpressionType> type = FeelTypecheck.typecheck(expression);

        assertEquals(new Either.Right<>(new ExpressionType.STRING()), type);
    }

    @ParameterizedTest
    @CsvSource({"true, BOOLEAN", "false, BOOLEAN"})
    void trueHasTypeBoolean(final String input, final String expectedType) {
        final FeelExpression expression = FeelParser.PARSER.parse(input);

        final Either<ValidationResult.Builder.ElementStep, ExpressionType> type = FeelTypecheck.typecheck(expression);

        assertEquals(ExpressionTypeParser.parse(expectedType, Collections.emptyList()), type);
    }

    @Test
    void dateHasTypeDate() {
        final FeelExpression expression = FeelParser.PARSER.parse("date(\"2015-11-30\")");

        final Either<ValidationResult.Builder.ElementStep, ExpressionType> type = FeelTypecheck.typecheck(expression);

        assertEquals(new Either.Right<>(new ExpressionType.DATE()), type);
    }

    @Test
    void dateTimeHasTypeDate() {
        final FeelExpression expression = FeelParser.PARSER.parse("date and time(\"2015-11-30T12:00:00\")");

        final Either<ValidationResult.Builder.ElementStep, ExpressionType> type = FeelTypecheck.typecheck(expression);

        assertEquals(new Either.Right<>(new ExpressionType.DATE()), type);
    }

    @Test
    void dateTimeHasTypeDate2() {
        final FeelExpression expression = FeelParser.PARSER.parse("date(?) > date(\"2015-11-30\")");
        final FeelTypecheck.Context context = new FeelTypecheck.Context();
        context.put("someVariable", new DATE());

        final Either<ValidationResult.Builder.ElementStep, ExpressionType> type = FeelTypecheck.typecheck(context, expression);

        assertEquals(new Either.Right<>(new ExpressionType.DATE()), type);
    }

    @Test
    void boundVariableHasType() {
        final FeelExpression expression = FeelParser.PARSER.parse("x");
        final FeelTypecheck.Context context = new FeelTypecheck.Context();
        context.put("x", new ExpressionType.INTEGER());

        final Either<ValidationResult.Builder.ElementStep, ExpressionType> type =
                FeelTypecheck.typecheck(context, expression);

        assertEquals(new Either.Right<>(new ExpressionType.INTEGER()), type);
    }

    @Test
    void unboundVariableHasNoType() {
        final FeelExpression expression = FeelParser.PARSER.parse("x");
        final FeelTypecheck.Context context = new FeelTypecheck.Context();
        final Either<ValidationResult.Builder.ElementStep, ExpressionType> type =
                FeelTypecheck.typecheck(context, expression);

        assertEquals(Optional.empty(), type.getRight());
        assertEquals(
                "Variable 'x' has no type.",
                type.getLeft().orElseThrow(AssertionError::new).getMessage());
    }

    @Test
    void questionMarkIsBoundIfAndOnlyIfThereIsOneVariableInTheContext() {
        final FeelExpression expression = FeelParser.PARSER.parse("?");
        final FeelTypecheck.Context context = new FeelTypecheck.Context();
        context.put("someVariable", new ExpressionType.INTEGER());

        final Either<ValidationResult.Builder.ElementStep, ExpressionType> type =
            FeelTypecheck.typecheck(context, expression);

        assertEquals(new Either.Right<>(new ExpressionType.INTEGER()), type);
    }

    @Test
    void questionMarkIsBoundIfTheOnlyVariableInContextIsNamedQuestionmark() {
        final FeelExpression expression = FeelParser.PARSER.parse("?");
        final FeelTypecheck.Context context = new FeelTypecheck.Context();
        context.put("?", new ExpressionType.INTEGER());

        final Either<ValidationResult.Builder.ElementStep, ExpressionType> type =
            FeelTypecheck.typecheck(context, expression);

        assertEquals(new Either.Right<>(new ExpressionType.INTEGER()), type);
    }

    @Test
    void questionMarkIsUnboundIfTheContextIsEmpty() {
        final FeelExpression expression = FeelParser.PARSER.parse("?");
        final FeelTypecheck.Context context = new FeelTypecheck.Context();
        final Either<ValidationResult.Builder.ElementStep, ExpressionType> type =
            FeelTypecheck.typecheck(context, expression);

        assertEquals(Optional.empty(), type.getRight());
        assertEquals(
            "Question mark can only be used if there is a variable in context.",
            type.getLeft().orElseThrow(AssertionError::new).getMessage());
    }

    @Test
    void questionMarkIsUnboundIfTheContextointainsMoreThanOneEntry() {
        final FeelExpression expression = FeelParser.PARSER.parse("?");
        final FeelTypecheck.Context context = new FeelTypecheck.Context();
        context.put("x", new INTEGER());
        context.put("y", new DATE());
        final Either<ValidationResult.Builder.ElementStep, ExpressionType> type =
            FeelTypecheck.typecheck(context, expression);

        assertEquals(Optional.empty(), type.getRight());
        assertEquals(
            "There is more than one variable in context. Question mark is ambiguous.",
            type.getLeft().orElseThrow(AssertionError::new).getMessage());
    }


    @ParameterizedTest
    @CsvSource({"<5, INTEGER", "<5.2, DOUBLE", "-1, INTEGER"})
    void lessThanExpressionHasNumericType(final String input, final String expectedType) {
        final FeelExpression expression = FeelParser.PARSER.parse(input);
        final Either<ValidationResult.Builder.ElementStep, ExpressionType> type = FeelTypecheck.typecheck(expression);

        assertEquals(ExpressionTypeParser.parse(expectedType, Collections.emptyList()), type);
    }

    @ParameterizedTest
    @CsvSource({
        "<\"Steak\", Operator < expects comparable type but got [STRING[]]",
        "2+\"foo\", 'Operator + expects numeric type but got [INTEGER[], STRING[]]'",
        "'<3,\"foo\"', Types of head and tail do not match.",
        "[1..1.5], Types of lower and upper bound do not match.",
        "[\"A\"..\"Z\"], Type is unsupported for RangeExpressions."
    })
    void isNotTypeable(final String input, final String errorMessage) {
        final FeelExpression expression = FeelParser.PARSER.parse(input);
        final Either<ValidationResult.Builder.ElementStep, ExpressionType> type = FeelTypecheck.typecheck(expression);

        assertEquals(Optional.empty(), type.getRight());
        assertEquals(
                errorMessage,
                type.getLeft().orElseThrow(AssertionError::new).getMessage());
    }

    @Test
    void notExpressionContainingTrueIsTypable() {
        final FeelExpression expression = FeelParser.PARSER.parse("not(true)");
        final Either<ValidationResult.Builder.ElementStep, ExpressionType> type = FeelTypecheck.typecheck(expression);

        assertEquals(new Either.Right<>(new ExpressionType.BOOLEAN()), type);
    }

    @Test
    void notExpressionContainingDisjunctionWithStringsIsTypable() {
        final FeelExpression expression = FeelParser.PARSER.parse("not(\"foo\", \"bar\")");
        final Either<ValidationResult.Builder.ElementStep, ExpressionType> type = FeelTypecheck.typecheck(expression);

        assertEquals(new Either.Right<>(new ExpressionType.STRING()), type);
    }
}
