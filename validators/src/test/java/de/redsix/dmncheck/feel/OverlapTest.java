package de.redsix.dmncheck.feel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class OverlapTest {

    @ParameterizedTest
    @CsvSource(
            delimiter = ';',
            value = {
                "'';1",
                "1;1",
                "1;1.0",
                "\"a\";\"a\"",
                "true;true",
                "null;null",
                "[1..5];[5..9]",
                "[1..5];3",
                "[1..5];[2..3]",
                "<= 8;[5..8]",
                "< 8;> 7",
                ">= 8;8",
                "<5;<3",
                "\"a\",\"b\";\"b\"",
                "not(1);2",
                "not(1);[1..2]",
                "date(\"2020-01-01\");[date(\"2019-01-01\")..date(\"2021-01-01\")]",
                "date and time(\"2020-01-01T10:00:00\");>= date and time(\"2020-01-01T10:00:00\")"
            })
    void overlappingExpressions(final String input, final String otherInput) {
        assertOverlap(Optional.of(true), input, otherInput);
    }

    @ParameterizedTest
    @CsvSource(
            delimiter = ';',
            value = {
                "1;2",
                "\"a\";\"b\"",
                "true;false",
                "null;1",
                "[1..5[;[5..9]",
                "[1..5];]5..9]",
                "[1..5];6",
                "< 8;>= 8",
                "< 8;8",
                "> 8;<= 8",
                "\"a\",\"b\";\"c\"",
                "not(1);1",
                "date(\"2020-01-01\");< date(\"2020-01-01\")"
            })
    void disjointExpressions(final String input, final String otherInput) {
        assertOverlap(Optional.of(false), input, otherInput);
    }

    @ParameterizedTest
    @CsvSource(
            delimiter = ';',
            value = {"foo;1", "1;\"a\"", "[1..x];3", "not(1);not(2)", "\"a\",x;\"b\"", "1 + 2;3", "?;1"})
    void undecidableExpressions(final String input, final String otherInput) {
        assertOverlap(Optional.empty(), input, otherInput);
    }

    private static void assertOverlap(final Optional<Boolean> expected, final String input, final String otherInput) {
        final FeelExpression expression = FeelParser.PARSER.parse(input);
        final FeelExpression otherExpression = FeelParser.PARSER.parse(otherInput);

        assertEquals(expected, expression.overlaps(otherExpression));
        assertEquals(expected, otherExpression.overlaps(expression));
    }
}
