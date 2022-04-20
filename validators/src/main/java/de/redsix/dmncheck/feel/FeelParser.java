package de.redsix.dmncheck.feel;

import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.util.Either;
import de.redsix.dmncheck.util.Eithers;
import de.redsix.dmncheck.util.Expression;
import org.camunda.bpm.model.dmn.impl.DmnModelConstants;
import org.jparsec.OperatorTable;
import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.jparsec.Scanners;
import org.jparsec.Terminals;
import org.jparsec.Tokens;
import org.jparsec.error.ParserException;
import org.jparsec.pattern.Patterns;

import java.time.LocalDateTime;
import java.util.Arrays;

public final class FeelParser {

    private FeelParser() {

    }

    private static final Terminals OPERATORS = Terminals
            .operators("+", "-", "*", "**", "/", "(", ")", "[", "]", "..", ",", "not(", "and", "or", "<", ">", "<=", ">=",
                    "date and time(\"", "\")");

    private static final Parser<Void> IGNORED = Scanners.WHITESPACES.skipMany();

    private static final Parser<?> TOKENIZER = Parsers.or(
            Patterns.regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}").toScanner("date").source()
                    .map(s -> Tokens.fragment(s, "datefragment")),
            Patterns.regex("^-$").toScanner("empty").source().map(s -> Tokens.fragment(s, "emptyfragment")),
            OPERATORS.tokenizer(),
            Patterns.regex("\"[^\"]*\"").toScanner("string").source().map(s -> Tokens.fragment(s, "stringfragment")),
            Patterns.string("true").or(Patterns.string("false")).toScanner("boolean").source()
                    .map(s -> Tokens.fragment(s, "booleanfragment")),
            Patterns.string("null").toScanner("null").source()
                    .map(s -> Tokens.fragment(s, "nullfragment")),
            Patterns.regex("([a-zA-Z_$][\\w$\\.]*)").toScanner("variable").source()
                    .map(s -> Tokens.fragment(s, "variablefragment")),
            Patterns.regex("[0-9]+\\.[0-9]+").toScanner("strict-decimal").source()
                    .map(s -> Tokens.fragment(s, Tokens.Tag.DECIMAL)),
            Terminals.IntegerLiteral.TOKENIZER.or(Terminals.IntegerLiteral.TOKENIZER)
    );

    private static final Parser<FeelExpression> INTEGER = Terminals.IntegerLiteral.PARSER.map(Integer::valueOf)
            .map(FeelExpressions::IntegerLiteral);

    private static final Parser<FeelExpression> DOUBLE = Terminals.DecimalLiteral.PARSER.map(Double::valueOf)
            .map(FeelExpressions::DoubleLiteral);

    private static final Parser<FeelExpression> VARIABLE = Terminals.fragment("variablefragment").map(FeelExpressions::VariableLiteral);

    private static final Parser<FeelExpression> STRING = Terminals.fragment("stringfragment")
            .map(s -> s.substring(1, s.length() -1 )).map(FeelExpressions::StringLiteral);

    private static final Parser<FeelExpression> BOOLEAN = Terminals.fragment("booleanfragment")
            .map(Boolean::valueOf).map(FeelExpressions::BooleanLiteral);

    private static final Parser<FeelExpression> NULL = Terminals.fragment("nullfragment")
                                                                .map(__ -> FeelExpressions.Null());

    private static final Parser<FeelExpression> DATE = Parsers.between(OPERATORS.token("date and time(\""),
            Terminals.fragment("datefragment").map(LocalDateTime::parse).map(FeelExpressions::DateLiteral), OPERATORS.token("\")"));

    private static Parser<FeelExpression> parseRangeExpression(final Parser<Boolean> leftBound, final Parser<FeelExpression> expression,
            final Parser<Boolean> rightBound) {
        return Parsers.sequence(leftBound, expression, OPERATORS.token("..").skipTimes(1), expression, rightBound,
                (isLeftInclusive, lowerBound, __, upperBound, isRightInclusive) -> FeelExpressions
                        .RangeExpression(isLeftInclusive, lowerBound, upperBound, isRightInclusive));
    }

    private static Parser<FeelExpression> createRangeExpressionParser(final Parser<FeelExpression> expressionParser) {
        return Parsers.or(
                parseRangeExpression(op("[", true), expressionParser, op("]", true)),
                parseRangeExpression(op("]", false), expressionParser, op("]", true)),
                parseRangeExpression(op("[", true), expressionParser, op("[", false)),
                parseRangeExpression(op("(", false), expressionParser, op("]", true)),
                parseRangeExpression(op("[", true), expressionParser, op(")", false)),
                parseRangeExpression(op("(", false), expressionParser, op(")", false)),
                parseRangeExpression(op("]", false), expressionParser, op(")", false)),
                parseRangeExpression(op("(", false), expressionParser, op("[", false)),
                parseRangeExpression(op("]", false), expressionParser, op("[", false))
        );
    }

    private static Parser<FeelExpression> createBinaryExpressionParser(final Parser<FeelExpression> feelExpressionParser) {
        return new OperatorTable<FeelExpression>()
                .infixr(op(",", FeelExpressions::DisjunctionExpression), 0)
                .prefix(op("<", v -> FeelExpressions.UnaryExpression(Operator.LT, v)), 5)
                .prefix(op(">", v -> FeelExpressions.UnaryExpression(Operator.GT, v)), 5)
                .prefix(op("<=", v -> FeelExpressions.UnaryExpression(Operator.LE, v)), 5)
                .prefix(op(">=", v -> FeelExpressions.UnaryExpression(Operator.GE, v)), 5)
                .infixl(op("or", (l, r) -> FeelExpressions.BinaryExpression(l, Operator.OR, r)), 8)
                .infixl(op("and", (l, r) -> FeelExpressions.BinaryExpression(l, Operator.AND, r)), 8)
                .infixl(op("+", (l, r) -> FeelExpressions.BinaryExpression(l, Operator.ADD, r)), 10)
                .infixl(op("-", (l, r) -> FeelExpressions.BinaryExpression(l, Operator.SUB, r)), 10)
                .infixl(op("*", (l, r) -> FeelExpressions.BinaryExpression(l, Operator.MUL, r)), 20)
                .infixl(op("**", (l, r) -> FeelExpressions.BinaryExpression(l, Operator.EXP, r)), 20)
                .infixl(op("/", (l, r) -> FeelExpressions.BinaryExpression(l, Operator.DIV, r)), 20)
                .prefix(op("-", v -> FeelExpressions.UnaryExpression(Operator.SUB, v)), 30)
                .build(feelExpressionParser);
    }

    private static Parser<FeelExpression> parseNot(Parser<FeelExpression> feelParserReference) {
        return Parsers.between(OPERATORS.token("not("), feelParserReference, OPERATORS.token(")")).map(expression -> {
            if (expression.containsNot()) {
                // TODO: How can this constraint be expressed in the grammar?
                throw new RuntimeException("Negations cannot be nested in FEEL expressions.");
            } else {
                return FeelExpressions.UnaryExpression(Operator.NOT, expression);
            }
        });
    }

    private static Parser<FeelExpression> parseEmpty() {
        return Parsers.EOF.map((__) -> FeelExpressions.Empty())
                .or(Terminals.fragment("emptyfragment").map((__) -> FeelExpressions.Empty()));
    }

    private static <T> Parser<T> op(final String name, final T value) {
        return OPERATORS.token(name).retn(value);
    }

    private static Parser<FeelExpression> feelExpressionParser() {
        final Parser.Reference<FeelExpression> feelParserReference = Parser.newReference();

        final Parser<FeelExpression> literalParser = Parsers.or(INTEGER, DOUBLE, BOOLEAN, VARIABLE, STRING, DATE);

        final Parser<FeelExpression> parseRangeExpression = createRangeExpressionParser(literalParser);

        final Parser<FeelExpression> feelExpressionParserWithoutBinaryExpressions = Parsers.or(literalParser, NULL,
                                                                                               parseNot(feelParserReference.lazy()),
                                                                                               parseRangeExpression);

        final Parser<FeelExpression> feelExpressionParser = createBinaryExpressionParser(feelExpressionParserWithoutBinaryExpressions);

        feelParserReference.set(feelExpressionParser);

        return Parsers.or(parseEmpty(), feelExpressionParser);
    }

    private static boolean expressionLanguageIsFeel(final String expressionLanguage) {
        return Arrays.asList(DmnModelConstants.FEEL_NS, DmnModelConstants.FEEL12_NS, DmnModelConstants.FEEL13_NS).contains(expressionLanguage)
                || expressionLanguage.equalsIgnoreCase("feel");
    }

    static final Parser<FeelExpression> PARSER = feelExpressionParser().from(TOKENIZER, IGNORED);

    public static Either<ValidationResult.Builder.ElementStep, FeelExpression> parse(final CharSequence charSequence) {
        try {
            return Eithers.right(PARSER.parse(charSequence));
        } catch (final ParserException e) {
            return Eithers.left(ValidationResult.init.message("Could not parse '" + charSequence + "': " + e.getMessage()));
        }
    }

    public static Either<ValidationResult.Builder.ElementStep, FeelExpression> parse(final Expression expression) {
        if (expressionLanguageIsFeel(expression.expressionLanguage)) {
            return parse(expression.textContent);
        } else {
            return Eithers.left(ValidationResult.init.message("Expression language '" +
                    expression.expressionLanguage + "' not supported").severity(Severity.WARNING));
        }
    }
}


