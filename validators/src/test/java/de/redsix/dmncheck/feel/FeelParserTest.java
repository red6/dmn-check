package de.redsix.dmncheck.feel;

import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.util.Either;
import de.redsix.dmncheck.util.Expression;
import java.time.LocalDate;
import java.util.List;
import org.camunda.bpm.model.dmn.impl.DmnModelConstants;
import org.camunda.bpm.model.dmn.instance.LiteralExpression;
import org.jparsec.error.ParserException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.time.Month;

import static de.redsix.dmncheck.feel.FeelExpression.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class FeelParserTest {

    @Test
    void shouldParseEmpty() {
        final FeelExpression expression = FeelParser.PARSER.parse("");

        final FeelExpression expectedExpression = new FeelExpression.Empty();

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseEmptyRepresentedAsDash() {
        final FeelExpression expression = FeelParser.PARSER.parse("-");

        final FeelExpression expectedExpression = new FeelExpression.Empty();

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseNumberLiterals() {
        final FeelExpression expression = FeelParser.PARSER.parse("42");

        final FeelExpression expectedExpression = new FeelExpression.IntegerLiteral(42);

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseVariableLiteral() {
        final FeelExpression expression = FeelParser.PARSER.parse("foobar");

        final FeelExpression expectedExpression = new FeelExpression.VariableLiteral("foobar");

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseDoubleLiteral() {
        final FeelExpression expression = FeelParser.PARSER.parse(String.valueOf(Math.PI));

        final FeelExpression expectedExpression = new FeelExpression.DoubleLiteral(Math.PI);

        assertEquals(expectedExpression, expression);
    }

    @ParameterizedTest
    @CsvSource({"true, true", "false, false"})
    void shouldParseBooleanLiteral(final String input, final boolean expectedValue) {
        final FeelExpression expression = FeelParser.PARSER.parse(input);

        final FeelExpression expectedExpression = new FeelExpression.BooleanLiteral(expectedValue);

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseNull() {
        final FeelExpression expression = FeelParser.PARSER.parse("null");

        final FeelExpression expectedExpression = new FeelExpression.Null();

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseVariableLiteralWithAttribute() {
        final FeelExpression expression = FeelParser.PARSER.parse("foo.bar");

        final FeelExpression expectedExpression = new FeelExpression.VariableLiteral("foo.bar");

        assertEquals(expectedExpression, expression);
    }

    @ParameterizedTest
    @CsvSource({"\"\", ''", "\"Steak\", Steak", "\"x.y.z=false\", x.y.z=false"})
    void shouldParseStringLiterals(final String input, final String expected) {
        final FeelExpression expression = FeelParser.PARSER.parse(input);

        final FeelExpression expectedExpression = new FeelExpression.StringLiteral(expected);

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseNegativeNumbers() {
        final FeelExpression expression = FeelParser.PARSER.parse("- 3");

        final FeelExpression expectedExpression = unaryExpression(Operator.SUB,
                new FeelExpression.IntegerLiteral(3));

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldNotParseDashWithinExpressionsAsEmpty() {
        assertThrows(ParserException.class, () -> FeelParser.PARSER.parse("2 * -"));
    }

    @Test
    void shouldHandleWhitespaceCorrectly() {
        final FeelExpression expression = FeelParser.PARSER.parse("2 * 3");

        final FeelExpression expectedExpression = FeelExpression.binaryExpression(Operator.MUL,
                new FeelExpression.IntegerLiteral(2), new FeelExpression.IntegerLiteral(3));

        assertEquals(expectedExpression, expression);
    }

    @ParameterizedTest
    @CsvSource({"2*3, MUL", "2+3, ADD", "2-3, SUB", "2**3, EXP", "2/3, DIV"})
    void shouldParseArithmeticExpressions(final String input, final Operator expectedOperator) {
        final FeelExpression expression = FeelParser.PARSER.parse(input);

        final FeelExpression expectedExpression = FeelExpression.binaryExpression(expectedOperator,
                new FeelExpression.IntegerLiteral(2), new FeelExpression.IntegerLiteral(3));

        assertEquals(expectedExpression, expression);
    }

    @ParameterizedTest
    @CsvSource({"true and false, AND", "true or false, OR"})
    void shouldParseConjunctionAndDisjunction(final String input, final Operator expectedOperator) {
        final FeelExpression expression = FeelParser.PARSER.parse(input);

        final FeelExpression expectedExpression = FeelExpression.binaryExpression(expectedOperator,
                new FeelExpression.BooleanLiteral(true), new FeelExpression.BooleanLiteral(false));

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseArithmeticExpressionWithVariable() {
        final FeelExpression expression = FeelParser.PARSER.parse("4 + x");

        final FeelExpression expectedExpression = FeelExpression.binaryExpression(Operator.ADD,
                new FeelExpression.IntegerLiteral(4), new FeelExpression.VariableLiteral("x"));

        assertEquals(expectedExpression, expression);
    }

    @ParameterizedTest
    @CsvSource({"<3, LT, 3", "< 3, LT, 3", "<=4, LE, 4", ">5, GT, 5", ">=6, GE, 6"})
    void shouldParseComparisonExpressions(final String input, final Operator operator, final int number) {
        final FeelExpression expression = FeelParser.PARSER.parse(input);

        final FeelExpression expectedExpression = unaryExpression(operator, new FeelExpression.IntegerLiteral(number));

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseComparisonExpressionWithVariable() {
        final FeelExpression expression = FeelParser.PARSER.parse("> y");

        final FeelExpression expectedExpression = unaryExpression(Operator.GT, new FeelExpression.VariableLiteral("y"));

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
    void shouldParseRangeExpression(
            final String input, final boolean isLeftInclusive, final int lowerBound, final int upperBound, final boolean isRightInclusive) {
        final FeelExpression expression = FeelParser.PARSER.parse(input);

        final FeelExpression expectedExpression = new FeelExpression.RangeExpression(
                isLeftInclusive, new FeelExpression.IntegerLiteral(lowerBound), new FeelExpression.IntegerLiteral(upperBound), isRightInclusive);

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseDoublesInRangeExpression() {
        final FeelExpression expression = FeelParser.PARSER.parse("[0.5..1.5]");

        final FeelExpression expectedExpression = new FeelExpression.RangeExpression(true, new FeelExpression.DoubleLiteral(0.5), new FeelExpression.DoubleLiteral(1.5), true);

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseRangeExpressionWithVariable() {
        final FeelExpression expression = FeelParser.PARSER.parse("[1..x]");

        final FeelExpression expectedExpression = new FeelExpression.RangeExpression(true, new FeelExpression.IntegerLiteral(1), new FeelExpression.VariableLiteral("x"), true);

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseDisjunctionExpressionWithComparisons() {
        final FeelExpression expression = FeelParser.PARSER.parse("<3,>100,42");

        final FeelExpression expectedExpression = new FeelExpression.DisjunctionExpression(
                unaryExpression(Operator.LT, new FeelExpression.IntegerLiteral(3)),
                new FeelExpression.DisjunctionExpression(unaryExpression(Operator.GT, new FeelExpression.IntegerLiteral(100)), new FeelExpression.IntegerLiteral(42)));

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseDisjunctionExpressionWithStrings() {
        final FeelExpression expression = FeelParser.PARSER.parse("\"Spareribs\",\"Steak\",\"Stew\"");

        final FeelExpression expectedExpression = new FeelExpression.DisjunctionExpression(
                new FeelExpression.StringLiteral("Spareribs"), new FeelExpression.DisjunctionExpression(new FeelExpression.StringLiteral("Steak"), new FeelExpression.StringLiteral("Stew")));

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseDisjunctionExpressionWithVariable() {
        final FeelExpression expression = FeelParser.PARSER.parse(">customer.age,>21");

        final FeelExpression expectedExpression = new FeelExpression.DisjunctionExpression(
                unaryExpression(Operator.GT, new FeelExpression.VariableLiteral("customer.age")),
                unaryExpression(Operator.GT, new FeelExpression.IntegerLiteral(21)));

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseDisjunctionExpressionWithRanges() {
        final FeelExpression expression = FeelParser.PARSER.parse("[1..2],[5..6],[8..10]");

        final FeelExpression expectedExpression = new FeelExpression.DisjunctionExpression(
                new FeelExpression.RangeExpression(true, new FeelExpression.IntegerLiteral(1), new FeelExpression.IntegerLiteral(2), true),
                new FeelExpression.DisjunctionExpression(
                        new FeelExpression.RangeExpression(true, new FeelExpression.IntegerLiteral(5), new FeelExpression.IntegerLiteral(6), true),
                        new FeelExpression.RangeExpression(true, new FeelExpression.IntegerLiteral(8), new FeelExpression.IntegerLiteral(10), true)));

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseDisjunctionWithMixedExpressions() {
        final FeelExpression expression = FeelParser.PARSER.parse("10,[20..30],>10,42");

        final FeelExpression expectedExpression = new FeelExpression.DisjunctionExpression(
                new FeelExpression.IntegerLiteral(10),
                new FeelExpression.DisjunctionExpression(
                        new FeelExpression.RangeExpression(true, new FeelExpression.IntegerLiteral(20), new FeelExpression.IntegerLiteral(30), true),
                        new FeelExpression.DisjunctionExpression(unaryExpression(Operator.GT, new FeelExpression.IntegerLiteral(10)), new FeelExpression.IntegerLiteral(42))));

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseExpressionContainingNot() {
        final FeelExpression expression = FeelParser.PARSER.parse("not([1..4],[6..9])");

        final FeelExpression expectedExpression = unaryExpression(
                Operator.NOT,
                new FeelExpression.DisjunctionExpression(
                        new FeelExpression.RangeExpression(true, new FeelExpression.IntegerLiteral(1), new FeelExpression.IntegerLiteral(4), true),
                        new FeelExpression.RangeExpression(true, new FeelExpression.IntegerLiteral(6), new FeelExpression.IntegerLiteral(9), true)));

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseDateExpression() {
        final FeelExpression expression = FeelParser.PARSER.parse("date(\"2015-11-30\")");

        final FeelExpression expectedExpression = unaryExpression(Operator.DATE, new DateLiteral(LocalDate.of(2015, Month.NOVEMBER, 30)));
        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseDateComparisonWithQuestionmarkOnLeftHandSide() {
        final FeelExpression expression = FeelParser.PARSER.parse("date(?) > date(\"2026-06-23\")");

        final FeelExpression expectedExpression = binaryExpression(Operator.GT,
            unaryExpression(Operator.DATE, new QuestionMark()),
            unaryExpression(Operator.DATE, new DateLiteral(LocalDate.of(2026, Month.JUNE, 23)))
        );
        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseDateComparisonWithQuestionmarkOnRightHandSide() {
        final FeelExpression expression = FeelParser.PARSER.parse("date(\"2026-06-23\") > date(?)");

        final FeelExpression expectedExpression = binaryExpression(Operator.GT,
            unaryExpression(Operator.DATE, new DateLiteral(LocalDate.of(2026, Month.JUNE, 23))),
            unaryExpression(Operator.DATE, new QuestionMark())
        );
        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseDateTimeExpression() {
        final FeelExpression expression = FeelParser.PARSER.parse("date and time(\"2015-11-30T12:00:00\")");

        final FeelExpression expectedExpression = unaryExpression(Operator.DATE_AND_TIME, new DateTimeLiteral(LocalDateTime.of(2015, Month.NOVEMBER, 30, 12, 0)));
        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseDateExpressionsInRange() {
        final FeelExpression expression = FeelParser.PARSER.parse(
            "[date(\"2015-11-30\")..date(\"2015-12-01\")]");

        final FeelExpression expectedExpression = new FeelExpression.RangeExpression(
            true,
            new NaryExpression(Operator.DATE, List.of(new DateLiteral(LocalDate.of(2015, Month.NOVEMBER, 30)))),
            new NaryExpression(Operator.DATE, List.of(new DateLiteral(LocalDate.of(2015, Month.DECEMBER, 1)))),
            true);
        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseDateExpressionsInRangeWithQuestionmark() {
        final FeelExpression expression = FeelParser.PARSER.parse(
            "[date(?)..date(\"2015-12-01\")]");

        final FeelExpression expectedExpression = new FeelExpression.RangeExpression(
            true,
            new NaryExpression(Operator.DATE, List.of(new QuestionMark())),
            new NaryExpression(Operator.DATE, List.of(new DateLiteral(LocalDate.of(2015, Month.DECEMBER, 1)))),
            true);
        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseDateTimeExpressionsInRange() {
        final FeelExpression expression = FeelParser.PARSER.parse(
                "[date and time(\"2015-11-30T12:00:00\")..date and time(\"2015-12-01T12:00:00\")]");

        final FeelExpression expectedExpression = new FeelExpression.RangeExpression(
                true,
                new NaryExpression(Operator.DATE_AND_TIME, List.of(new DateTimeLiteral(LocalDateTime.of(2015, Month.NOVEMBER, 30, 12, 0)))),
                new NaryExpression(Operator.DATE_AND_TIME, List.of(new DateTimeLiteral(LocalDateTime.of(2015, Month.DECEMBER, 1, 12, 0)))),
                true);
        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldTreatParsingErrorsAsValidationErrors() {
        final Either<ValidationResult.Builder.ElementStep, FeelExpression> result = FeelParser.parse("[1..");

        final String expectedErrorMessage = "Could not parse '[1..': line 1, column 5:\n"
                + "<, >, <=, >=, -, questionmarkfragment, INTEGER, DECIMAL, booleanfragment, variablefragment, stringfragment, datetimefragment, datefragment, nullfragment, builtinfragment, [, ] or ( expected, EOF encountered.";

        assertTrue(result.getLeft().isPresent());
        assertEquals(expectedErrorMessage, result.getLeft().get().getMessage());
    }

    @Test
    void shouldWarnIfExpressionLanguageIsNotFeel() {
        final LiteralExpression literalExpression = Mockito.mock(LiteralExpression.class);
        when(literalExpression.getExpressionLanguage()).thenReturn("javascript");
        final Expression expression = new Expression(literalExpression, null);

        final Either<ValidationResult.Builder.ElementStep, FeelExpression> result = FeelParser.parse(expression);
        final String expectedErrorMessage = "Expression language 'javascript' not supported";

        assertTrue(result.getLeft().isPresent());
        assertEquals(expectedErrorMessage, result.getLeft().get().getMessage());
    }

    @Test
    void shouldParseEmptyFromExpression() {
        final LiteralExpression literalExpression = Mockito.mock(LiteralExpression.class);
        when(literalExpression.getExpressionLanguage()).thenReturn(DmnModelConstants.FEEL_NS);
        when(literalExpression.getTextContent()).thenReturn("");

        final Expression expression = new Expression(literalExpression, null);

        final Either<ValidationResult.Builder.ElementStep, FeelExpression> result = FeelParser.parse(expression);

        assertTrue(result.getRight().isPresent());
        assertEquals(new FeelExpression.Empty(), result.getRight().get());
    }
}
