package de.redsix.dmncheck.feel;

import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.util.Either;
import de.redsix.dmncheck.util.Eithers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Collections;
import java.util.Optional;

import static de.redsix.dmncheck.util.Eithers.right;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FeelTypecheckTest {

    @Test
    void emptyHasTypeTop() {
        final FeelExpression expression = FeelParser.PARSER.parse("");
        final Either<ValidationResult.Builder.ElementStep, ExpressionType> type = FeelTypecheck.typecheck(expression);

        assertEquals(right(ExpressionTypes.TOP()), type);
    }

    @Test
    void integerHasTypeInteger() {
        final FeelExpression expression = FeelParser.PARSER.parse("42");
        final Either<ValidationResult.Builder.ElementStep, ExpressionType> type = FeelTypecheck.typecheck(expression);

        assertEquals(right(ExpressionTypes.INTEGER()), type);
    }

    @Test
    void doubleHasTypeDouble() {
        final FeelExpression expression = FeelParser.PARSER.parse("3.14159265359");
        final Either<ValidationResult.Builder.ElementStep, ExpressionType> type = FeelTypecheck.typecheck(expression);

        assertEquals(right(ExpressionTypes.DOUBLE()), type);
    }

    @Test
    void stringHasTypeString() {
        final FeelExpression expression = FeelParser.PARSER.parse("\"Steak\"");

        final Either<ValidationResult.Builder.ElementStep, ExpressionType> type = FeelTypecheck.typecheck(expression);

        assertEquals(right(ExpressionTypes.STRING()), type);
    }

    @ParameterizedTest
    @CsvSource({"true, BOOLEAN", "false, BOOLEAN"})
    void trueHasTypeBoolean(String input, String expectedType) {
        final FeelExpression expression = FeelParser.PARSER.parse(input);

        final Either<ValidationResult.Builder.ElementStep, ExpressionType> type = FeelTypecheck.typecheck(expression);

        assertEquals(ExpressionTypeParser.parse(expectedType, Collections.emptyList()), type);
    }

    @Test
    void dateHasTypeDate() {
        final FeelExpression expression = FeelParser.PARSER.parse("date and time(\"2015-11-30T12:00:00\")");

        final Either<ValidationResult.Builder.ElementStep, ExpressionType> type = FeelTypecheck.typecheck(expression);

        assertEquals(right(ExpressionTypes.DATE()), type);
    }

    @Test
    void boundVariableHasType() {
        final FeelExpression expression = FeelParser.PARSER.parse("x");
        final FeelTypecheck.Context context = new FeelTypecheck.Context();
        context.put("x", ExpressionTypes.INTEGER());

        final Either<ValidationResult.Builder.ElementStep, ExpressionType> type = FeelTypecheck.typecheck(context, expression);

        assertEquals(right(ExpressionTypes.INTEGER()), type);
    }

    @Test
    void unboundVariableHasNoType() {
        final FeelExpression expression = FeelParser.PARSER.parse("x");
        final FeelTypecheck.Context context = new FeelTypecheck.Context();
        final Either<ValidationResult.Builder.ElementStep, ExpressionType> type = FeelTypecheck.typecheck(context, expression);

        assertEquals(Optional.empty(), Eithers.getRight(type));
        assertEquals("Variable 'x' has no type.", Eithers.getLeft(type).orElseThrow(AssertionError::new).getMessage());
    }

    @ParameterizedTest
    @CsvSource({"<5, INTEGER", "<5.2, DOUBLE", "-1, INTEGER"})
    void lessThanExpressionHasNumericType(String input, String expectedType) {
        final FeelExpression expression = FeelParser.PARSER.parse(input);
        final Either<ValidationResult.Builder.ElementStep, ExpressionType> type = FeelTypecheck.typecheck(expression);

        assertEquals(ExpressionTypeParser.parse(expectedType, Collections.emptyList()), type);
    }

    @Test
    void lessThanExpressionIsNotTypeableForStrings() {
        final FeelExpression expression = FeelParser.PARSER.parse("<\"Steak\"");
        final Either<ValidationResult.Builder.ElementStep, ExpressionType> type = FeelTypecheck.typecheck(expression);

        assertEquals(Optional.empty(), Eithers.getRight(type));
        assertEquals("Operator < expects numeric type but got " + ExpressionTypes.STRING(),
                Eithers.getLeft(type).orElseThrow(AssertionError::new).getMessage());
    }

    @Test
    void additionOfIntegersIsTypeable() {
        final FeelExpression expression = FeelParser.PARSER.parse("3+4");
        final Either<ValidationResult.Builder.ElementStep, ExpressionType> type = FeelTypecheck.typecheck(expression);

        assertEquals(right(ExpressionTypes.INTEGER()), type);
    }

    @Test
    void additionOfIntegerAndStringIsNotTypeable() {
        final FeelExpression expression = FeelParser.PARSER.parse("2+\"foo\"");
        final Either<ValidationResult.Builder.ElementStep, ExpressionType> type = FeelTypecheck.typecheck(expression);

        assertEquals(Optional.empty(), Eithers.getRight(type));
        assertEquals("Types of left and right operand do not match.",
                Eithers.getLeft(type).orElseThrow(AssertionError::new).getMessage());
    }

    @Test
    void disjunctionOfComparisonsIsTypeable() {
        final FeelExpression expression = FeelParser.PARSER.parse("<3,>8");
        final Either<ValidationResult.Builder.ElementStep, ExpressionType> type = FeelTypecheck.typecheck(expression);

        assertEquals(right(ExpressionTypes.INTEGER()), type);
    }

    @Test
    void disjunctionOfComparisonAndStringIsNotTypeable() {
        final FeelExpression expression = FeelParser.PARSER.parse("<3,\"foo\"");
        final Either<ValidationResult.Builder.ElementStep, ExpressionType> type = FeelTypecheck.typecheck(expression);

        assertEquals(Optional.empty(), Eithers.getRight(type));
        assertEquals("Types of head and tail do not match.",
                Eithers.getLeft(type).orElseThrow(AssertionError::new).getMessage());
    }

    @Test
    void rangeOfIntegersIsWellTyped() {
        final FeelExpression expression = FeelParser.PARSER.parse("[3..42]");
        final Either<ValidationResult.Builder.ElementStep, ExpressionType> type = FeelTypecheck.typecheck(expression);

        assertEquals(right(ExpressionTypes.INTEGER()), type);
    }

    @Test
    void rangeFromIntegerToDoubleIsNotTypeable() {
        final FeelExpression expression = FeelParser.PARSER.parse("[1..1.5]");
        final Either<ValidationResult.Builder.ElementStep, ExpressionType> type = FeelTypecheck.typecheck(expression);

        assertEquals(Optional.empty(), Eithers.getRight(type));
        assertEquals("Types of lower and upper bound do not match.",
                Eithers.getLeft(type).orElseThrow(AssertionError::new).getMessage());
    }

    @Test
    void rangeExpressionContainingStringsIsIlltyped() {
        final FeelExpression expression = FeelParser.PARSER.parse("[\"A\"..\"Z\"]");
        final Either<ValidationResult.Builder.ElementStep, ExpressionType> type = FeelTypecheck.typecheck(expression);

        assertEquals(Optional.empty(), Eithers.getRight(type));
        assertEquals("Type is unsupported for RangeExpressions.",
                Eithers.getLeft(type).orElseThrow(AssertionError::new).getMessage());
    }

    @Test
    void notExpressionContainingTrueIsTypable() {
        final FeelExpression expression = FeelParser.PARSER.parse("not(true)");
        final Either<ValidationResult.Builder.ElementStep, ExpressionType> type = FeelTypecheck.typecheck(expression);

        assertEquals(right(ExpressionTypes.BOOLEAN()), type);
    }

    @Test
    void notExpressionContainingDisjunctionWithStringsIsTypable() {
        final FeelExpression expression = FeelParser.PARSER.parse("not(\"foo\", \"bar\")");
        final Either<ValidationResult.Builder.ElementStep, ExpressionType> type = FeelTypecheck.typecheck(expression);

        assertEquals(right(ExpressionTypes.STRING()), type);
    }
}
