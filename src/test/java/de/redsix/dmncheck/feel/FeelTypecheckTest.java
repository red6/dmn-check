package de.redsix.dmncheck.feel;

import de.redsix.dmncheck.model.ExpressionTypeEnum;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.util.Either;
import de.redsix.dmncheck.util.Eithers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Optional;

import static de.redsix.dmncheck.util.Eithers.left;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FeelTypecheckTest {

    @Test
    public void integerHasTypeInteger() {
        final FeelExpression expression = FeelParser.PARSER.parse("42");
        final Either<ExpressionTypeEnum, ValidationResult.Builder> type = FeelTypecheck.typecheck(expression);

        assertEquals(left(ExpressionTypeEnum.INTEGER), type);
    }

    @Test
    public void doubleHasTypeDouble() {
        final FeelExpression expression = FeelParser.PARSER.parse("3.14159265359");
        final Either<ExpressionTypeEnum, ValidationResult.Builder> type = FeelTypecheck.typecheck(expression);

        assertEquals(left(ExpressionTypeEnum.DOUBLE), type);
    }

    @Test
    public void stringHasTypeString() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("\"Steak\"");

        final Either<ExpressionTypeEnum, ValidationResult.Builder> type = FeelTypecheck.typecheck(expression);

        assertEquals(left(ExpressionTypeEnum.STRING), type);
    }

    @ParameterizedTest
    @CsvSource({"true, BOOLEAN", "false, BOOLEAN"})
    public void trueHasTypeBoolean(String input, ExpressionTypeEnum expectedType) throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse(input);

        final Either<ExpressionTypeEnum, ValidationResult.Builder> type = FeelTypecheck.typecheck(expression);

        assertEquals(left(expectedType), type);
    }

    @Test
    public void dateHasTypeDate() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("date and time(\"2015-11-30T12:00:00\")");

        final Either<ExpressionTypeEnum, ValidationResult.Builder> type = FeelTypecheck.typecheck(expression);

        assertEquals(left(ExpressionTypeEnum.DATE), type);
    }

    @Test
    public void boundVariableHasType() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("x");
        final FeelTypecheck.Context context = new FeelTypecheck.Context();
        context.put("x", ExpressionTypeEnum.INTEGER);

        final Either<ExpressionTypeEnum, ValidationResult.Builder> type = FeelTypecheck.typecheck(context, expression);

        assertEquals(left(ExpressionTypeEnum.INTEGER), type);
    }

    @Test
    public void unboundVariableHasNoType() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("x");
        final FeelTypecheck.Context context = new FeelTypecheck.Context();
        final Either<ExpressionTypeEnum, ValidationResult.Builder> type = FeelTypecheck.typecheck(context, expression);

        assertEquals(Optional.empty(), Eithers.getLeft(type));
        assertEquals("Variable 'x' has no type.", Eithers.getRight(type).orElseThrow(AssertionError::new).message);
    }

    @ParameterizedTest
    @CsvSource({"<5, INTEGER", "<5.2, DOUBLE"})
    public void lessThanExpressionHasNumericType(String input, ExpressionTypeEnum expectedType) throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse(input);
        final Either<ExpressionTypeEnum, ValidationResult.Builder> type = FeelTypecheck.typecheck(expression);

        assertEquals(left(expectedType), type);
    }

    @Test
    public void lessThanExpressionIsNotTypeableForStrings() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("<\"Steak\"");
        final Either<ExpressionTypeEnum, ValidationResult.Builder> type = FeelTypecheck.typecheck(expression);

        assertEquals(Optional.empty(), Eithers.getLeft(type));
        assertEquals("Expression has wrong type.", Eithers.getRight(type).orElseThrow(AssertionError::new).message);
    }

    @Test
    public void additionOfIntegersIsTypeable() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("3+4");
        final Either<ExpressionTypeEnum, ValidationResult.Builder> type = FeelTypecheck.typecheck(expression);

        assertEquals(left(ExpressionTypeEnum.INTEGER), type);
    }

    @Test
    public void additionOfIntegerAndStringIsNotTypeable() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("2+\"foo\"");
        final Either<ExpressionTypeEnum, ValidationResult.Builder> type = FeelTypecheck.typecheck(expression);

        assertEquals(Optional.empty(), Eithers.getLeft(type));
        assertEquals("Types of left and right operand do not match.",
                Eithers.getRight(type).orElseThrow(AssertionError::new).message);
    }

    @Test
    public void disjunctionOfComparisonsIsTypeable() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("<3,>8");
        final Either<ExpressionTypeEnum, ValidationResult.Builder> type = FeelTypecheck.typecheck(expression);

        assertEquals(left(ExpressionTypeEnum.INTEGER), type);
    }

    @Test
    public void disjunctionOfComparisonAndStringIsNotTypeable() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("<3,\"foo\"");
        final Either<ExpressionTypeEnum, ValidationResult.Builder> type = FeelTypecheck.typecheck(expression);

        assertEquals(Optional.empty(), Eithers.getLeft(type));
        assertEquals("Types of head and tail do not match.",
                Eithers.getRight(type).orElseThrow(AssertionError::new).message);
    }
}
