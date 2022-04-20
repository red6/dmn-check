package de.redsix.dmncheck.feel;

import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.util.Either;
import de.redsix.dmncheck.util.Eithers;
import de.redsix.dmncheck.util.Expression;
import org.camunda.bpm.model.dmn.impl.DmnModelConstants;
import org.camunda.bpm.model.dmn.instance.LiteralExpression;
import org.jparsec.error.ParserException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.time.Month;

import static de.redsix.dmncheck.feel.FeelExpressions.BinaryExpression;
import static de.redsix.dmncheck.feel.FeelExpressions.BooleanLiteral;
import static de.redsix.dmncheck.feel.FeelExpressions.DateLiteral;
import static de.redsix.dmncheck.feel.FeelExpressions.DisjunctionExpression;
import static de.redsix.dmncheck.feel.FeelExpressions.DoubleLiteral;
import static de.redsix.dmncheck.feel.FeelExpressions.Empty;
import static de.redsix.dmncheck.feel.FeelExpressions.IntegerLiteral;
import static de.redsix.dmncheck.feel.FeelExpressions.Null;
import static de.redsix.dmncheck.feel.FeelExpressions.RangeExpression;
import static de.redsix.dmncheck.feel.FeelExpressions.StringLiteral;
import static de.redsix.dmncheck.feel.FeelExpressions.UnaryExpression;
import static de.redsix.dmncheck.feel.FeelExpressions.VariableLiteral;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class FeelParserTest {

    @Test
    void shouldParseEmpty() {
        final FeelExpression expression = FeelParser.PARSER.parse("");

        final FeelExpression expectedExpression = Empty();

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseEmptyRepresentedAsDash() {
        final FeelExpression expression = FeelParser.PARSER.parse("-");

        final FeelExpression expectedExpression = Empty();

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseNumberLiterals() {
        final FeelExpression expression = FeelParser.PARSER.parse("42");

        final FeelExpression expectedExpression = IntegerLiteral(42);

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseVariableLiteral() {
        final FeelExpression expression = FeelParser.PARSER.parse("foobar");

        final FeelExpression expectedExpression = VariableLiteral("foobar");

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseDoubleLiteral() {
        final FeelExpression expression = FeelParser.PARSER.parse(String.valueOf(Math.PI));

        final FeelExpression expectedExpression = FeelExpressions.DoubleLiteral(Math.PI);

        assertEquals(expectedExpression, expression);
    }

    @ParameterizedTest
    @CsvSource({"true, true", "false, false"})
    void shouldParseBooleanLiteral(String input, boolean expectedValue) {
        final FeelExpression expression = FeelParser.PARSER.parse(input);

        final FeelExpression expectedExpression = FeelExpressions.BooleanLiteral(expectedValue);

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseNull() {
        final FeelExpression expression = FeelParser.PARSER.parse("null");

        final FeelExpression expectedExpression = Null();

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseVariableLiteralWithAttribute() {
        final FeelExpression expression = FeelParser.PARSER.parse("foo.bar");

        final FeelExpression expectedExpression = VariableLiteral("foo.bar");

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseEmptyStringLiteral() {
        final FeelExpression expression = FeelParser.PARSER.parse("\"\"");

        final FeelExpression expectedExpression = FeelExpressions.StringLiteral("");

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseStringLiteral() {
        final FeelExpression expression = FeelParser.PARSER.parse("\"Steak\"");

        final FeelExpression expectedExpression = FeelExpressions.StringLiteral("Steak");

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseStringLiteralContainingSpecialCharacters() {
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
    void shouldNotParseDashWithinExpressionsAsEmpty() {
        assertThrows(ParserException.class, () -> FeelParser.PARSER.parse("2 * -"));
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
    void shouldParseArithmeticExpressions(String input, Operator expectedOperator) {
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
    void shouldParseArithmeticExpressionWithVariable() {
        final FeelExpression expression = FeelParser.PARSER.parse("4 + x");

        final FeelExpression expectedExpression = BinaryExpression(
                IntegerLiteral(4), Operator.ADD, VariableLiteral("x")
        );

        assertEquals(expectedExpression, expression);
    }

    @ParameterizedTest
    @CsvSource({"<3, LT, 3", "< 3, LT, 3", "<=4, LE, 4", ">5, GT, 5", ">=6, GE, 6"})
    void shouldParseComparisonExpressions(String input, Operator operator, int number) {
        final FeelExpression expression = FeelParser.PARSER.parse(input);

        final FeelExpression expectedExpression = UnaryExpression(operator, IntegerLiteral(number));

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseComparisonExpressionWithVariable() {
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
    void shouldParseRangeExpression(String input, boolean isLeftInclusive, int lowerBound, int upperBound, boolean isRightInclusive) {
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
    void shouldParseRangeExpressionWithVariable() {
        final FeelExpression expression = FeelParser.PARSER.parse("[1..x]");

        final FeelExpression expectedExpression = RangeExpression(true, IntegerLiteral(1),
                VariableLiteral("x"), true);

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseDisjunctionExpressionWithComparisons() {
        final FeelExpression expression = FeelParser.PARSER.parse("<3,>100,42");

        final FeelExpression expectedExpression = DisjunctionExpression(
                UnaryExpression(Operator.LT, IntegerLiteral(3)),
                DisjunctionExpression(UnaryExpression(Operator.GT, IntegerLiteral(100)),
                        IntegerLiteral(42)));

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseDisjunctionExpressionWithStrings() {
        final FeelExpression expression = FeelParser.PARSER.parse("\"Spareribs\",\"Steak\",\"Stew\"");

        final FeelExpression expectedExpression = DisjunctionExpression(
                StringLiteral("Spareribs"),
                DisjunctionExpression(StringLiteral("Steak"), StringLiteral("Stew")));

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseDisjunctionExpressionWithVariable() {
        final FeelExpression expression = FeelParser.PARSER.parse(">customer.age,>21");

        final FeelExpression expectedExpression = DisjunctionExpression(
                UnaryExpression(Operator.GT, VariableLiteral("customer.age")),
                UnaryExpression(Operator.GT, IntegerLiteral(21)));

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseDisjunctionExpressionWithRanges() {
        final FeelExpression expression = FeelParser.PARSER.parse("[1..2],[5..6],[8..10]");

        final FeelExpression expectedExpression = DisjunctionExpression(
                RangeExpression(true, IntegerLiteral(1), IntegerLiteral(2), true),
                DisjunctionExpression(
                        RangeExpression(true, IntegerLiteral(5), IntegerLiteral(6), true),
                        RangeExpression(true, IntegerLiteral(8), IntegerLiteral(10), true)));

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseDisjunctionWithMixedExpressions() {
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
    void shouldParseExpressionContainingNot() {
        final FeelExpression expression = FeelParser.PARSER.parse("not([1..4],[6..9])");

        final FeelExpression expectedExpression = UnaryExpression(Operator.NOT,
                DisjunctionExpression(
                        RangeExpression(true, IntegerLiteral(1), IntegerLiteral(4), true),
                        RangeExpression(true, IntegerLiteral(6), IntegerLiteral(9), true)));

        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldNotParseNestedNegations() {
        final Throwable throwable = assertThrows(ParserException.class, () -> FeelParser.PARSER.parse("not(not(true))"));
        assertEquals("Negations cannot be nested in FEEL expressions.\nline 1, column 15", throwable.getMessage());
    }

    @Test
    void shouldParseDateExpression() {
        final FeelExpression expression = FeelParser.PARSER.parse("date and time(\"2015-11-30T12:00:00\")");

        final FeelExpression expectedExpression = DateLiteral(LocalDateTime.of(2015, Month.NOVEMBER, 30, 12, 0));
        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldParseDateExpressionsInRange() {
        final FeelExpression expression = FeelParser.PARSER.parse(
                "[date and time(\"2015-11-30T12:00:00\")..date and time(\"2015-12-01T12:00:00\")]");

        final FeelExpression expectedExpression = RangeExpression(true,
                DateLiteral(LocalDateTime.of(2015, Month.NOVEMBER, 30, 12, 0)),
                DateLiteral(LocalDateTime.of(2015, Month.DECEMBER, 1, 12, 0)), true);
        assertEquals(expectedExpression, expression);
    }

    @Test
    void shouldTreatParsingErrorsAsValidationErrors() {
        final Either<ValidationResult.Builder.ElementStep, FeelExpression> result = FeelParser.parse("[1..");

        final String expectedErrorMessage = "Could not parse '[1..': line 1, column 5:\n"
                + "INTEGER, DECIMAL, booleanfragment, variablefragment, stringfragment or date and time(\" expected, EOF encountered.";

        assertTrue(Eithers.getLeft(result).isPresent());
        assertEquals(expectedErrorMessage, Eithers.getLeft(result).get().getMessage());
    }

    @Test
    void shouldWarnIfExpressionLanguageIsNotFeel() {
        final LiteralExpression literalExpression = Mockito.mock(LiteralExpression.class);
        when(literalExpression.getExpressionLanguage()).thenReturn("javascript");
        final Expression expression = new Expression(literalExpression, null);

        final Either<ValidationResult.Builder.ElementStep, FeelExpression> result = FeelParser.parse(expression);
        final String expectedErrorMessage = "Expression language 'javascript' not supported";

        assertTrue(Eithers.getLeft(result).isPresent());
        assertEquals(expectedErrorMessage, Eithers.getLeft(result).get().getMessage());
    }

    @Test
    void shouldParseEmptyFromExpression() {
        final LiteralExpression literalExpression = Mockito.mock(LiteralExpression.class);
        when(literalExpression.getExpressionLanguage()).thenReturn(DmnModelConstants.FEEL_NS);
        when(literalExpression.getTextContent()).thenReturn("");

        final Expression expression = new Expression(literalExpression, null);

        final Either<ValidationResult.Builder.ElementStep, FeelExpression> result = FeelParser.parse(expression);

        assertTrue(Eithers.getRight(result).isPresent());
        assertEquals(FeelExpressions.Empty(), Eithers.getRight(result).get());
    }
}
