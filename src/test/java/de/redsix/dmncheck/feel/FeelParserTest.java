package de.redsix.dmncheck.feel;

import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.util.Either;
import de.redsix.dmncheck.util.Eithers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;
import java.time.Month;

import static de.redsix.dmncheck.feel.FeelExpressions.BinaryExpression;
import static de.redsix.dmncheck.feel.FeelExpressions.BooleanLiteral;
import static de.redsix.dmncheck.feel.FeelExpressions.DateLiteral;
import static de.redsix.dmncheck.feel.FeelExpressions.DisjunctionExpression;
import static de.redsix.dmncheck.feel.FeelExpressions.DoubleLiteral;
import static de.redsix.dmncheck.feel.FeelExpressions.Empty;
import static de.redsix.dmncheck.feel.FeelExpressions.IntegerLiteral;
import static de.redsix.dmncheck.feel.FeelExpressions.RangeExpression;
import static de.redsix.dmncheck.feel.FeelExpressions.StringLiteral;
import static de.redsix.dmncheck.feel.FeelExpressions.UnaryExpression;
import static de.redsix.dmncheck.feel.FeelExpressions.VariableLiteral;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeelParserTest {

    @Test
    void shouldParseEmpty() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("");

        final FeelExpression expectedExpression = Empty();

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseEmptyRepresentedAsDash() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("-");

        final FeelExpression expectedExpression = Empty();

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseNumberLiterals() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("42");

        final FeelExpression expectedExpression = IntegerLiteral(42);

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseVariableLiteral() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("foobar");

        final FeelExpression expectedExpression = VariableLiteral("foobar");

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseDoubleLiteral() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("3.14159265359");

        final FeelExpression expectedExpression = FeelExpressions.DoubleLiteral(3.14159265359);

        assertEquals(expectedExpression, expression);
    }

    @ParameterizedTest
    @CsvSource({"true, true", "false, false"})
    void shouldParseBooleanLiteral(String input, boolean expectedValue) throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse(input);

        final FeelExpression expectedExpression = FeelExpressions.BooleanLiteral(expectedValue);

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseVariableLiteralWithAttribute() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("foo.bar");

        final FeelExpression expectedExpression = VariableLiteral("foo.bar");

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseStringLiteral() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("\"Steak\"");

        final FeelExpression expectedExpression = FeelExpressions.StringLiteral("Steak");

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseStringLiteralContainingSpecialCharacters() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("\"x.y.z=false\"");

        final FeelExpression expectedExpression = FeelExpressions.StringLiteral("x.y.z=false");

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseNegativeNumbers() {
        final FeelExpression expression = FeelParser.PARSER.parse("- 3");

        final FeelExpression expectedExpression = UnaryExpression(Operator.SUB, IntegerLiteral(3));

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldRespectPrecedenceOfMinus() {
        final FeelExpression expression = FeelParser.PARSER.parse("2 * -3");

        final FeelExpression expectedExpression = BinaryExpression(
                IntegerLiteral(2), Operator.MUL, UnaryExpression(Operator.SUB, IntegerLiteral(3))
        );

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldHandleWhitespaceCorrectly() {
        final FeelExpression expression = FeelParser.PARSER.parse("2 * 3");

        final FeelExpression expectedExpression = BinaryExpression(
                IntegerLiteral(2), Operator.MUL, IntegerLiteral(3)
        );

        assertEquals(expectedExpression, expression);
    }

    @ParameterizedTest
    @CsvSource({"2*3, MUL", "2+3, ADD", "2-3, SUB", "2**3, EXP", "2/3, DIV"})
    void shouldParseArithmeticExpressions(String input, Operator expectedOperator) throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse(input);

        final FeelExpression expectedExpression = BinaryExpression(
                IntegerLiteral(2), expectedOperator, IntegerLiteral(3)
        );

        assertEquals(expectedExpression, expression);
    }

    @ParameterizedTest
    @CsvSource({"true and false, AND", "true or false, OR"})
    void shouldParseConjunctionAndDisjunction(String input, Operator expectedOperator) {
        final FeelExpression expression = FeelParser.PARSER.parse(input);

        final FeelExpression expectedExpression = BinaryExpression(
                BooleanLiteral(true), expectedOperator, BooleanLiteral(false)
        );

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseArithmeticExpressionWithVariable() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("4 + x");

        final FeelExpression expectedExpression = BinaryExpression(
                IntegerLiteral(4), Operator.ADD, VariableLiteral("x")
        );

        assertEquals(expectedExpression, expression);
    }

    @ParameterizedTest
    @CsvSource({"<3, LT, 3", "< 3, LT, 3", "<=4, LE, 4", ">5, GT, 5", ">=6, GE, 6"})
    void shouldParseComparisonExpressions(String input, Operator operator, int number) throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse(input);

        final FeelExpression expectedExpression = UnaryExpression(operator, IntegerLiteral(number));

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseComparisonExpressionWithVariable() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("> y");

        final FeelExpression expectedExpression = UnaryExpression(Operator.GT, VariableLiteral("y"));

        assertEquals(expectedExpression, expression);
    }

    @ParameterizedTest
    @CsvSource({
            "[1..2], true, 1, 2, true",
            "(1..2], false, 1, 2, true",
            "[1..2), true, 1, 2, false",
            "[1..2[, true, 1, 2, false",
            "]1..2], false, 1, 2, true",
            "(1..2), false, 1, 2, false",
            "]1..2), false, 1, 2, false",
            "]1..2[, false, 1, 2, false",
            "(1..2[, false, 1, 2, false"
    })
    void shouldParseRangeExpression(String input, boolean isLeftInclusive, int lowerBound, int upperBound, boolean isRightInclusive) throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse(input);

        final FeelExpression expectedExpression = RangeExpression(isLeftInclusive, IntegerLiteral(lowerBound),
                IntegerLiteral(upperBound), isRightInclusive);

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseDoublesInRangeExpression() {
        final FeelExpression expression = FeelParser.PARSER.parse("[0.5..1.5]");

        final FeelExpression expectedExpression = RangeExpression(true, DoubleLiteral(0.5),
                DoubleLiteral(1.5), true);

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseRangeExpressionWithVariable() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("[1..x]");

        final FeelExpression expectedExpression = RangeExpression(true, IntegerLiteral(1),
                VariableLiteral("x"), true);

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseDisjunctionExpressionWithComparisons() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("<3,>100,42");

        final FeelExpression expectedExpression = DisjunctionExpression(
                UnaryExpression(Operator.LT, IntegerLiteral(3)),
                DisjunctionExpression(UnaryExpression(Operator.GT, IntegerLiteral(100)),
                        IntegerLiteral(42)));

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseDisjunctionExpressionWithStrings() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("\"Spareribs\",\"Steak\",\"Stew\"");

        final FeelExpression expectedExpression = DisjunctionExpression(
                StringLiteral("Spareribs"),
                DisjunctionExpression(StringLiteral("Steak"), StringLiteral("Stew")));

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseDisjunctionExpressionWithVariable() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse(">customer.age,>21");

        final FeelExpression expectedExpression = DisjunctionExpression(
                UnaryExpression(Operator.GT, VariableLiteral("customer.age")),
                UnaryExpression(Operator.GT, IntegerLiteral(21)));

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseDisjunctionExpressionWithRanges() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("[1..2],[5..6],[8..10]");

        final FeelExpression expectedExpression = DisjunctionExpression(
                RangeExpression(true, IntegerLiteral(1), IntegerLiteral(2), true),
                DisjunctionExpression(
                        RangeExpression(true, IntegerLiteral(5), IntegerLiteral(6), true),
                        RangeExpression(true, IntegerLiteral(8), IntegerLiteral(10), true)));

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseDisjunctionWithMixedExpressions() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("10,[20..30],>10,42");

        final FeelExpression expectedExpression = DisjunctionExpression(
                IntegerLiteral(10),
                DisjunctionExpression(
                        RangeExpression(true, IntegerLiteral(20), IntegerLiteral(30), true),
                        DisjunctionExpression(
                                UnaryExpression(Operator.GT, IntegerLiteral(10)),
                                IntegerLiteral(42))));

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseExpressionContainingNot() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("not([1..4],[6..9])");

        final FeelExpression expectedExpression = UnaryExpression(Operator.NOT,
                DisjunctionExpression(
                        RangeExpression(true, IntegerLiteral(1), IntegerLiteral(4), true),
                        RangeExpression(true, IntegerLiteral(6), IntegerLiteral(9), true)));

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseDateExpression() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("date and time(\"2015-11-30T12:00:00\")");

        final FeelExpression expectedExpression = DateLiteral(LocalDateTime.of(2015, Month.NOVEMBER, 30, 12, 0));
        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseDateExpressionsInRange() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse(
                "[date and time(\"2015-11-30T12:00:00\")..date and time(\"2015-12-01T12:00:00\")]");

        final FeelExpression expectedExpression = RangeExpression(true,
                DateLiteral(LocalDateTime.of(2015, Month.NOVEMBER, 30, 12, 0)),
                DateLiteral(LocalDateTime.of(2015, Month.DECEMBER, 1, 12, 0)), true);
        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldTreatParsingErrorsAsValidationErrors() throws Exception {
        final Either<FeelExpression, ValidationResult.Builder> result = FeelParser.parse("[1..");

        final String expectedErrorMessage = "Could not parse '[1..': line 1, column 5:\n"
                + "<, >, <=, >=, -, INTEGER, DECIMAL, booleanfragment, variablefragment, stringfragment, date and time(\", not(, [, ] or ( expected, EOF encountered.";

        assertTrue(Eithers.getRight(result).isPresent());
        assertEquals(expectedErrorMessage, Eithers.getRight(result).get().message);
    }
}
