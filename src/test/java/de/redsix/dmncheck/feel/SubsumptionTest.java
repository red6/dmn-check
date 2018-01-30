package de.redsix.dmncheck.feel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SubsumptionTest {

    @ParameterizedTest
    @CsvSource({"1", "\"a\"", "[1..2]", "<3"})
    public void emptySubsumesEverything(final String input) throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse(input);
        final FeelExpression emptyExpression = FeelExpressions.Empty();

        assertEquals(Optional.of(true), Subsumption.subsumes(emptyExpression, expression, Subsumption.Comparison.EQ));
    }

    @ParameterizedTest
    @CsvSource({"1", "\"a\"", "[1..2]", "<3"})
    public void nothingSubsumesEmptyExceptEmpty(final String input) throws Exception {
        final FeelExpression expression = FeelParser.PARSER.parse(input);
        final FeelExpression emptyExpression = FeelExpressions.Empty();

        assertEquals(Optional.of(false), Subsumption.subsumes(expression, emptyExpression, Subsumption.Comparison.EQ));
    }

    @Test
    public void emptySubsumesEmpty() {
        final FeelExpression emptyExpression = FeelExpressions.Empty();
        assertEquals(Optional.of(true), Subsumption.subsumes(emptyExpression, emptyExpression, Subsumption.Comparison.EQ));
    }

    @ParameterizedTest
    @CsvSource({"[1..2], [1..2]", "[1..9], [4..5]", "[1..5], (1..5)", "[1..5], [4..5]", "[1..5], [1..2]"})
    public void rangeExpressionsThatSubsumeEachOther(final String subsumingInput, final String subsumedInput) {
        final FeelExpression subsumingExpression = FeelParser.PARSER.parse(subsumingInput);
        final FeelExpression subsumedExpression = FeelParser.PARSER.parse(subsumedInput);

        assertEquals(Optional.of(true), Subsumption.subsumes(subsumingExpression, subsumedExpression, Subsumption.Comparison.EQ));
    }

    @ParameterizedTest
    @CsvSource({"[5..5], [1..9]", "(1..5), [1..5]", "[4..5], [1..5]", "[1..2], [1..5]"})
    public void rangeExpressionsThatDoNotSubsumeEachOther(final String subsumingInput, final String subsumedInput) {
        final FeelExpression subsumingExpression = FeelParser.PARSER.parse(subsumingInput);
        final FeelExpression subsumedExpression = FeelParser.PARSER.parse(subsumedInput);

        assertEquals(Optional.of(false), Subsumption.subsumes(subsumingExpression, subsumedExpression, Subsumption.Comparison.EQ));
    }

    @ParameterizedTest
    @CsvSource({"<5, [1..5)", "<5, [1..4]", "<=5, [1..5]", ">5, (5..9]", ">5, [6..9]", ">=5, [5..9]"})
    public void comparisonExpressionsThatSubsumesRangeExpression(final String subsumingInput, final String subsumedInput) {
        final FeelExpression subsumingExpression = FeelParser.PARSER.parse(subsumingInput);
        final FeelExpression subsumedExpression = FeelParser.PARSER.parse(subsumedInput);

        assertEquals(Optional.of(true), Subsumption.subsumes(subsumingExpression, subsumedExpression, Subsumption.Comparison.EQ));
    }

    @ParameterizedTest
    @CsvSource({"<=5, <5", "<=5, <=5", ">=5, >5", ">=5, >=5", ">1, >5", "<5, <1",
                "<=5.0, <5.0", "<=5.0, <=5.0", ">=5.0, >5.0", ">=5.0, >=5.0", ">1.0, >5.0", "<5.0, <1.0",
                "<=date and time(\"2015-11-30T12:00:00\"), <date and time(\"2015-11-30T12:00:00\")", "<=date and time(\"2015-11-30T12:00:00\"), <=date and time(\"2015-11-30T12:00:00\")", ">=date and time(\"2015-11-30T12:00:00\"), >date and time(\"2015-11-30T12:00:00\")", ">=date and time(\"2015-11-30T12:00:00\"), >=date and time(\"2015-11-30T12:00:00\")", ">date and time(\"2014-11-30T12:00:00\"), >date and time(\"2015-11-30T12:00:00\")", "<date and time(\"2015-11-30T12:00:00\"), <date and time(\"2014-11-30T12:00:00\")"})
    public void comparisonExpressionsThatSubsumesComparisonExpression(final String subsumingInput, final String subsumedInput) {
        final FeelExpression subsumingExpression = FeelParser.PARSER.parse(subsumingInput);
        final FeelExpression subsumedExpression = FeelParser.PARSER.parse(subsumedInput);

        assertEquals(Optional.of(true), Subsumption.subsumes(subsumingExpression, subsumedExpression, Subsumption.Comparison.EQ));
    }

    @ParameterizedTest
    @CsvSource({"<5, <=5", ">5, >=5", ">5, >1", "<1, <5",
                "<5.0, <=5.0", ">5.0, >=5.0", ">5.0, >1.0", "<1.0, <5.0",
                "<date and time(\"2015-11-30T12:00:00\"), <=date and time(\"2015-11-30T12:00:00\")", ">date and time(\"2015-11-30T12:00:00\"), >=date and time(\"2015-11-30T12:00:00\")", ">date and time(\"2015-11-30T12:00:00\"), >date and time(\"2014-11-30T12:00:00\")", "<date and time(\"2014-11-30T12:00:00\"), <date and time(\"2015-11-30T12:00:00\")"})
    public void comparisonExpressionsThatDoNotSubsumesComparisonExpression(final String subsumingInput, final String subsumedInput) {
        final FeelExpression subsumingExpression = FeelParser.PARSER.parse(subsumingInput);
        final FeelExpression subsumedExpression = FeelParser.PARSER.parse(subsumedInput);

        assertEquals(Optional.of(false), Subsumption.subsumes(subsumingExpression, subsumedExpression, Subsumption.Comparison.EQ));
    }

    @ParameterizedTest
    @CsvSource({"true, true", "false, false"})
    public void subsumptionForBooleanIsEqualityPositiveCases(final String subsumingInput, final String subsumedInput) {
        final FeelExpression subsumingExpression = FeelParser.PARSER.parse(subsumingInput);
        final FeelExpression subsumedExpression = FeelParser.PARSER.parse(subsumedInput);

        assertEquals(Optional.of(true), Subsumption.subsumes(subsumingExpression, subsumedExpression, Subsumption.Comparison.EQ));
    }

    @ParameterizedTest
    @CsvSource({"true, false", "false, true"})
    public void subsumptionForBooleanIsEqualityNegativeCases(final String subsumingInput, final String subsumedInput) {
        final FeelExpression subsumingExpression = FeelParser.PARSER.parse(subsumingInput);
        final FeelExpression subsumedExpression = FeelParser.PARSER.parse(subsumedInput);

        assertEquals(Optional.of(false), Subsumption.subsumes(subsumingExpression, subsumedExpression, Subsumption.Comparison.EQ));
    }

}
