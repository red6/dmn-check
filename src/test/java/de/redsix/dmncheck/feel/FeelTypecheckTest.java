package de.redsix.dmncheck.feel;

import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.util.Either;
import de.redsix.dmncheck.util.Eithers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Optional;

import static de.redsix.dmncheck.util.Eithers.left;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FeelTypecheckTest {

    @Test
    void emptyHasTypeTop() {
        final FeelExpression expression = FeelParser.PARSER.parse("");
        final Either<ExpressionType, ValidationResult.Builder.ElementStep> type = FeelTypecheck.typecheck(expression);

        assertEquals(left(ExpressionType.TOP), type);
    }

    @Test
    void integerHasTypeInteger() {
        final FeelExpression expression = FeelParser.PARSER.parse("42");
        final Either<ExpressionType, ValidationResult.Builder.ElementStep> type = FeelTypecheck.typecheck(expression);

        assertEquals(left(ExpressionType.INTEGER), type);
    }

    @Test
    void doubleHasTypeDouble() {
        final FeelExpression expression = FeelParser.PARSER.parse("3.14159265359");
        final Either<ExpressionType, ValidationResult.Builder.ElementStep> type = FeelTypecheck.typecheck(expression);

        assertEquals(left(ExpressionType.DOUBLE), type);
    }

    @Test
    void stringHasTypeString() {
        final FeelExpression expression = FeelParser.PARSER.parse("\"Steak\"");

        final Either<ExpressionType, ValidationResult.Builder.ElementStep> type = FeelTypecheck.typecheck(expression);

        assertEquals(left(ExpressionType.STRING), type);
    }

    @ParameterizedTest
    @CsvSource({"true, BOOLEAN", "false, BOOLEAN"})
    void trueHasTypeBoolean(String input, ExpressionType expectedType) {
        final FeelExpression expression = FeelParser.PARSER.parse(input);

        final Either<ExpressionType, ValidationResult.Builder.ElementStep> type = FeelTypecheck.typecheck(expression);

        assertEquals(left(expectedType), type);
    }

    @Test
    void dateHasTypeDate() {
        final FeelExpression expression = FeelParser.PARSER.parse("date and time(\"2015-11-30T12:00:00\")");

        final Either<ExpressionType, ValidationResult.Builder.ElementStep> type = FeelTypecheck.typecheck(expression);

        assertEquals(left(ExpressionType.DATE), type);
    }

    @Test
    void boundVariableHasType() {
        final FeelExpression expression = FeelParser.PARSER.parse("x");
        final FeelTypecheck.Context context = new FeelTypecheck.Context();
        context.put("x", ExpressionType.INTEGER);

        final Either<ExpressionType, ValidationResult.Builder.ElementStep> type = FeelTypecheck.typecheck(context, expression);

        assertEquals(left(ExpressionType.INTEGER), type);
    }

    @Test
    void unboundVariableHasNoType() {
        final FeelExpression expression = FeelParser.PARSER.parse("x");
        final FeelTypecheck.Context context = new FeelTypecheck.Context();
        final Either<ExpressionType, ValidationResult.Builder.ElementStep> type = FeelTypecheck.typecheck(context, expression);

        assertEquals(Optional.empty(), Eithers.getLeft(type));
        assertEquals("Variable 'x' has no type.", Eithers.getRight(type).orElseThrow(AssertionError::new).getMessage());
    }

    @ParameterizedTest
    @CsvSource({"<5, INTEGER", "<5.2, DOUBLE"})
    void lessThanExpressionHasNumericType(String input, ExpressionType expectedType) {
        final FeelExpression expression = FeelParser.PARSER.parse(input);
        final Either<ExpressionType, ValidationResult.Builder.ElementStep> type = FeelTypecheck.typecheck(expression);

        assertEquals(left(expectedType), type);
    }

    @Test
    void lessThanExpressionIsNotTypeableForStrings() {
        final FeelExpression expression = FeelParser.PARSER.parse("<\"Steak\"");
        final Either<ExpressionType, ValidationResult.Builder.ElementStep> type = FeelTypecheck.typecheck(expression);

        assertEquals(Optional.empty(), Eithers.getLeft(type));
        assertEquals("Non-numeric type in UnaryExpression.", Eithers.getRight(type).orElseThrow(AssertionError::new).getMessage());
    }

    @Test
    void additionOfIntegersIsTypeable() {
        final FeelExpression expression = FeelParser.PARSER.parse("3+4");
        final Either<ExpressionType, ValidationResult.Builder.ElementStep> type = FeelTypecheck.typecheck(expression);

        assertEquals(left(ExpressionType.INTEGER), type);
    }

    @Test
    void additionOfIntegerAndStringIsNotTypeable() {
        final FeelExpression expression = FeelParser.PARSER.parse("2+\"foo\"");
        final Either<ExpressionType, ValidationResult.Builder.ElementStep> type = FeelTypecheck.typecheck(expression);

        assertEquals(Optional.empty(), Eithers.getLeft(type));
        assertEquals("Types of left and right operand do not match.",
                Eithers.getRight(type).orElseThrow(AssertionError::new).getMessage());
    }

    @Test
    void disjunctionOfComparisonsIsTypeable() {
        final FeelExpression expression = FeelParser.PARSER.parse("<3,>8");
        final Either<ExpressionType, ValidationResult.Builder.ElementStep> type = FeelTypecheck.typecheck(expression);

        assertEquals(left(ExpressionType.INTEGER), type);
    }

    @Test
    void disjunctionOfComparisonAndStringIsNotTypeable() {
        final FeelExpression expression = FeelParser.PARSER.parse("<3,\"foo\"");
        final Either<ExpressionType, ValidationResult.Builder.ElementStep> type = FeelTypecheck.typecheck(expression);

        assertEquals(Optional.empty(), Eithers.getLeft(type));
        assertEquals("Types of head and tail do not match.",
                Eithers.getRight(type).orElseThrow(AssertionError::new).getMessage());
    }

    @Test
    void rangeOfIntegersIsWellTyped() {
        final FeelExpression expression = FeelParser.PARSER.parse("[3..42]");
        final Either<ExpressionType, ValidationResult.Builder.ElementStep> type = FeelTypecheck.typecheck(expression);

        assertEquals(left(ExpressionType.INTEGER), type);
    }

    @Test
    void rangeFromIntegerToDoubleIsNotTypeable() {
        final FeelExpression expression = FeelParser.PARSER.parse("[1..1.5]");
        final Either<ExpressionType, ValidationResult.Builder.ElementStep> type = FeelTypecheck.typecheck(expression);

        assertEquals(Optional.empty(), Eithers.getLeft(type));
        assertEquals("Types of lower and upper bound do not match.",
                Eithers.getRight(type).orElseThrow(AssertionError::new).getMessage());
    }

    @Test
    void rangeExpressionContainingStringsIsIlltyped() {
        final FeelExpression expression = FeelParser.PARSER.parse("[\"A\"..\"Z\"]");
        final Either<ExpressionType, ValidationResult.Builder.ElementStep> type = FeelTypecheck.typecheck(expression);

        assertEquals(Optional.empty(), Eithers.getLeft(type));
        assertEquals("Type is unsupported for RangeExpressions.",
                Eithers.getRight(type).orElseThrow(AssertionError::new).getMessage());
    }
}
