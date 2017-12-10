package de.redsix.dmncheck.feel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.time.Month;

import static de.redsix.dmncheck.feel.FeelExpressions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FeelParserTest {

    @Test
    public void shouldParseNumberLiterals() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("42");

        final FeelExpression expectedExpression = IntegerLiteral(42);

        assertEquals(expectedExpression, expression);
    }

    @Test
    public void shouldParseVariableLiteral() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("foobar");

        final FeelExpression expectedExpression = VariableLiteral("foobar");

        assertEquals(expectedExpression, expression);
    }

    @Test
    public void shouldParseDoubleLiteral() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("3.14159265359");

        final FeelExpression expectedExpression = FeelExpressions.DoubleLiteral(3.14159265359);

        assertEquals(expectedExpression, expression);
    }

    @ParameterizedTest
    @CsvSource({"true, true", "false, false"})
    public void shouldParseBooleanLiteral(String input, boolean expectedValue) throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse(input);

        final FeelExpression expectedExpression = FeelExpressions.BooleanLiteral(expectedValue);

        assertEquals(expectedExpression, expression);
    }

    @Test
    public void shouldParseVariableLiteralWithAttribute() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("foo.bar");

        final FeelExpression expectedExpression = VariableLiteral("foo.bar");

        assertEquals(expectedExpression, expression);
    }

    @Test
    public void shouldParseStringLiteral() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("\"Steak\"");

        final FeelExpression expectedExpression = FeelExpressions.StringLiteral("Steak");

        assertEquals(expectedExpression, expression);
    }


    @ParameterizedTest
    @ValueSource(strings = {"2*3", "2  * 3"})
    public void shouldParseArithmeticExpressions(String input) throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse(input);

        final FeelExpression expectedExpression = BinaryExpression(
                IntegerLiteral(2), Operator.MUL, IntegerLiteral(3)
        );

        assertEquals(expectedExpression, expression);
    }

    @Test
    public void shouldParseArithmeticExpressionWithVariable() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("4 + x");

        final FeelExpression expectedExpression = BinaryExpression(
                IntegerLiteral(4), Operator.ADD, VariableLiteral("x")
        );

        assertEquals(expectedExpression, expression);
    }

    @ParameterizedTest
    @CsvSource({"<3, LT, 3", "< 3, LT, 3", "<=4, LE, 4", ">5, GT, 5", ">=6, GE, 6"})
    public void shouldParseComparisonExpressions(String input, Operator operator, int number) throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse(input);

        final FeelExpression expecedExpression = UnaryExpression(operator, IntegerLiteral(number));

        assertEquals(expecedExpression, expression);
    }

    @Test
    public void shouldParseComparisonExpressionWithVariable() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("> y");

        final FeelExpression expecedExpression = UnaryExpression(Operator.GT, VariableLiteral("y"));

        assertEquals(expecedExpression, expression);
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
    public void shouldParseRangeExpression(String input, boolean isLeftInclusive, int lowerBound, int upperBound, boolean isRightInclusive) throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse(input);

        final FeelExpression expectedExpression = RangeExpression(isLeftInclusive, IntegerLiteral(lowerBound),
                IntegerLiteral(upperBound), isRightInclusive);

        assertEquals(expectedExpression, expression);
    }

    @Test
    public void shouldParseRangeExpressionWithVariable() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("[1..x]");

        final FeelExpression expectedExpression = RangeExpression(true, IntegerLiteral(1),
                VariableLiteral("x"), true);

        assertEquals(expectedExpression, expression);
    }

    @Test
    public void shouldParseDisjunctionExpressionWithComparsions() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("<3,>100,42");

        final FeelExpression expectedExpression = DisjunctionExpression(
                UnaryExpression(Operator.LT, IntegerLiteral(3)),
                DisjunctionExpression(UnaryExpression(Operator.GT, IntegerLiteral(100)),
                        IntegerLiteral(42)));

        assertEquals(expectedExpression, expression);
    }

    @Test
    public void shouldParseDisjunctionExpressionWithStrings() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("\"Spareribs\",\"Steak\",\"Stew\"");

        final FeelExpression expectedExpression = DisjunctionExpression(
                StringLiteral("Spareribs"),
                DisjunctionExpression(StringLiteral("Steak"), StringLiteral("Stew")));

        assertEquals(expectedExpression, expression);
    }

    @Test
    public void shouldParseDisjunctionExpressionWithVariable() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse(">customer.age,>21");

        final FeelExpression expectedExpression = DisjunctionExpression(
                UnaryExpression(Operator.GT, VariableLiteral("customer.age")),
                UnaryExpression(Operator.GT, IntegerLiteral(21)));

        assertEquals(expectedExpression, expression);
    }

    @Test
    public void shouldParseDisjunctionExpressionWithRanges() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("[1..2],[5..6],[8..10]");

        final FeelExpression expectedExpression = DisjunctionExpression(
                RangeExpression(true, IntegerLiteral(1), IntegerLiteral(2), true),
                DisjunctionExpression(
                        RangeExpression(true, IntegerLiteral(5), IntegerLiteral(6), true),
                        RangeExpression(true, IntegerLiteral(8), IntegerLiteral(10), true)));

        assertEquals(expectedExpression, expression);
    }

    @Test
    public void shouldParseDisjunctionWithMixedExpressions() throws Exception {
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
    public void shouldParseExpressionContainingNot() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("not([1..4],[6..9])");

        final FeelExpression expectedExpression = UnaryExpression(Operator.NOT,
                DisjunctionExpression(
                        RangeExpression(true, IntegerLiteral(1), IntegerLiteral(4), true),
                        RangeExpression(true, IntegerLiteral(6), IntegerLiteral(9), true)));

        assertEquals(expectedExpression, expression);
    }

    @Test
    public void shouldParseDateExpression() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("date and time(\"2015-11-30T12:00:00\")");

        final FeelExpression expectedExpression = DateLiteral(LocalDateTime.of(2015, Month.NOVEMBER, 30, 12, 0));
        assertEquals(expectedExpression, expression);
    }

    @Test
    public void shouldParseDateExpressionsInRange() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse(
                "[date and time(\"2015-11-30T12:00:00\")..date and time(\"2015-12-01T12:00:00\")]");

        final FeelExpression expectedExpression = RangeExpression(true,
                DateLiteral(LocalDateTime.of(2015, Month.NOVEMBER, 30, 12, 0)),
                DateLiteral(LocalDateTime.of(2015, Month.DECEMBER, 1, 12, 0)), true);
        assertEquals(expectedExpression, expression);
    }
}
