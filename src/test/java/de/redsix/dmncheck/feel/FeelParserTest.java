package de.redsix.dmncheck.feel;

import de.redsix.dmncheck.feel.FeelParser;
import de.redsix.dmncheck.feel.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FeelParserTest {

    @Test
    public void shouldParseNumberLiterals() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("42");

        final IntegerLiteral expectedExpression = new IntegerLiteral(42);

        assertEquals(expectedExpression, expression);
    }

    @Test
    public void shouldParseVariableLiteral() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("foobar");

        final VariableLiteral expectedExpression = new VariableLiteral("foobar");

        assertEquals(expectedExpression, expression);
    }

    @Test
    public void shouldParseDoubleLiteral() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("3.14159265359");

        final DoubleLiteral expectedExpression = new DoubleLiteral(3.14159265359);

        assertEquals(expectedExpression, expression);
    }

    @ParameterizedTest
    @CsvSource({"true, true", "false, false"})
    public void shouldParseBooleanLiteral(String input, boolean expectedValue) throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse(input);

        final BooleanLiteral expectedExpression = new BooleanLiteral(expectedValue);

        assertEquals(expectedExpression, expression);
    }

    @Test
    public void shouldParseVariableLiteralWithAttribute() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("foo.bar");

        final VariableLiteral expectedExpression = new VariableLiteral("foo.bar");

        assertEquals(expectedExpression, expression);
    }

    @Test
    public void shouldParseStringLiteral() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("\"Steak\"");

        final StringLiteral expectedExpression = new StringLiteral("Steak");

        assertEquals(expectedExpression, expression);
    }


    @ParameterizedTest
    @ValueSource(strings = {"2*3", "2  * 3"})
    public void shouldParseArithmeticExpressions(String input) throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse(input);

        final BinaryExpression expectedExpression = new BinaryExpression(
                new IntegerLiteral(2), Operator.MUL, new IntegerLiteral(3)
        );

        assertEquals(expectedExpression, expression);
    }

    @Test
    public void shouldParseArithmeticExpressionWithVariable() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("4 + x");

        final BinaryExpression expectedExpression = new BinaryExpression(
                new IntegerLiteral(4), Operator.ADD, new VariableLiteral("x")
        );

        assertEquals(expectedExpression, expression);
    }

    @ParameterizedTest
    @CsvSource({"<3, LT, 3", "< 3, LT, 3", "<=4, LE, 4", ">5, GT, 5", ">=6, GE, 6"})
    public void shouldParseComparisonExpressions(String input, Operator operator, int number) throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse(input);

        final UnaryExpression expecedExpression = new UnaryExpression(operator, new IntegerLiteral(number));

        assertEquals(expecedExpression, expression);
    }

    @Test
    public void shouldParseComparisonExpressionWithVariable() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("> y");

        final UnaryExpression expecedExpression = new UnaryExpression(Operator.GT, new VariableLiteral("y"));

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

        final RangeExpression expectedExpression = new RangeExpression(isLeftInclusive, new IntegerLiteral(lowerBound), null,
                new IntegerLiteral(upperBound), isRightInclusive);

        assertEquals(expectedExpression, expression);
    }

    @Test
    public void shouldParseRangeExpressionWithVariable() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("[1..x]");

        final RangeExpression expectedExpression = new RangeExpression(true, new IntegerLiteral(1), null,
                new VariableLiteral("x"), true);

        assertEquals(expectedExpression, expression);
    }

    @Test
    public void shouldParseDisjunctionExpressionWithComparsions() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("<3,>100,42");

        final DisjunctionExpression expectedExpression = new DisjunctionExpression(
                new UnaryExpression(Operator.LT, new IntegerLiteral(3)),
                new DisjunctionExpression(new UnaryExpression(Operator.GT, new IntegerLiteral(100)),
                        new IntegerLiteral(42)));

        assertEquals(expectedExpression, expression);
    }

    @Test
    public void shouldParseDisjunctionExpressionWithStrings() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("\"Spareribs\",\"Steak\",\"Stew\"");

        final DisjunctionExpression expectedExpression = new DisjunctionExpression(
                new StringLiteral("Spareribs"),
                    new DisjunctionExpression(new StringLiteral("Steak"), new StringLiteral("Stew")));

        assertEquals(expectedExpression, expression);
    }

    @Test
    public void shouldParseDisjunctionExpressionWithVariable() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse(">customer.age,>21");

        final DisjunctionExpression expectedExpression = new DisjunctionExpression(
                new UnaryExpression(Operator.GT, new VariableLiteral("customer.age")),
                new UnaryExpression(Operator.GT, new IntegerLiteral(21)));

        assertEquals(expectedExpression, expression);
    }

    @Test
    public void shouldParseDisjunctionExpressionWithRanges() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("[1..2],[5..6],[8..10]");

        final DisjunctionExpression expectedExpression = new DisjunctionExpression(
                new RangeExpression(true, new IntegerLiteral(1), null, new IntegerLiteral(2), true),
                new DisjunctionExpression(
                        new RangeExpression(true, new IntegerLiteral(5), null, new IntegerLiteral(6), true),
                        new RangeExpression(true, new IntegerLiteral(8), null, new IntegerLiteral(10), true)));

        assertEquals(expectedExpression, expression);
    }

    @Test
    public void shouldParseDisjunctionWithMixedExpressions() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("10,[20..30],>10,42");

        final DisjunctionExpression expectedExpression = new DisjunctionExpression(
                new IntegerLiteral(10),
                new DisjunctionExpression(
                        new RangeExpression(true, new IntegerLiteral(20), null, new IntegerLiteral(30), true),
                            new DisjunctionExpression(
                                new UnaryExpression(Operator.GT, new IntegerLiteral(10)),
                                new IntegerLiteral(42))));

        assertEquals(expectedExpression, expression);
    }

    @Test
    public void shouldParseExpressionContainingNot() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("not([1..4],[6..9])");

        final UnaryExpression expectedExpression = new UnaryExpression(Operator.NOT,
                new DisjunctionExpression(
                        new RangeExpression(true, new IntegerLiteral(1), null, new IntegerLiteral(4), true),
                        new RangeExpression(true, new IntegerLiteral(6), null, new IntegerLiteral(9), true)));

        assertEquals(expectedExpression, expression);
    }

    @Test
    public void shouldParseDateExpression() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse("date and time(\"2015-11-30T12:00:00\")");

        final DateLiteral expectedExpression = new DateLiteral(LocalDateTime.of(2015, Month.NOVEMBER, 30, 12, 0));
        assertEquals(expectedExpression, expression);
    }

    @Test
    public void shouldParseDateExpressionsInRange() throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse(
                "[date and time(\"2015-11-30T12:00:00\")..date and time(\"2015-12-01T12:00:00\")]");

        final RangeExpression expectedExpression = new RangeExpression(true,
                new DateLiteral(LocalDateTime.of(2015, Month.NOVEMBER, 30, 12, 0)), null,
                new DateLiteral(LocalDateTime.of(2015, Month.DECEMBER, 1, 12, 0)), true);
        assertEquals(expectedExpression, expression);
    }
}
