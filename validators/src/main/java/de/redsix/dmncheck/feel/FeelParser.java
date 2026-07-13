package de.redsix.dmncheck.feel;

import de.redsix.dmncheck.feel.FeelExpression.DateLiteral;
import de.redsix.dmncheck.feel.FeelExpression.DateTimeLiteral;
import de.redsix.dmncheck.feel.FeelExpression.NaryExpression;
import de.redsix.dmncheck.feel.FeelExpression.QuestionMark;
import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.util.Either;
import de.redsix.dmncheck.util.Expression;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import org.camunda.bpm.model.dmn.impl.DmnModelConstants;
import org.jparsec.OperatorTable;
import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.jparsec.Scanners;
import org.jparsec.Terminals;
import org.jparsec.Tokens;
import org.jparsec.error.ParserException;
import org.jparsec.pattern.Patterns;

public final class FeelParser {

    private FeelParser() {}

    private static final Terminals OPERATORS = Terminals.operators(
            "+",
            "-",
            "*",
            "**",
            "/",
            "(",
            ")",
            "[",
            "]",
            "..",
            ",",
            "and",
            "or",
            "<",
            ">",
            "<=",
            ">=",
            "?");

    private static final Terminals BUILTINS = Terminals.operators(
        "date and time",
        "date",
        "not"
    );

    private static final Parser<Void> IGNORED = Scanners.WHITESPACES.skipMany();

    private static final Parser<?> TOKENIZER = Parsers.or(
        Patterns.string("?")
                    .toScanner("questionmark")
                    .source()
                    .map(s -> Tokens.fragment(s, "questionmarkfragment")),
        Patterns.regex("\"\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\"")
                    .toScanner("date_time")
                    .source()
                    .map(s -> Tokens.fragment(s.substring(1, s.length() - 1), "datetimefragment")),
            Patterns.regex("\"\\d{4}-\\d{2}-\\d{2}\"")
                    .toScanner("date")
                    .source()
                    .map(s -> Tokens.fragment(s.substring(1, s.length() - 1), "datefragment")),
            Patterns.regex("^-$").toScanner("empty").source().map(s -> Tokens.fragment(s, "emptyfragment")),
        BUILTINS.tokenizer()
            .source()
            .map(s -> Tokens.fragment(s, "builtinfragment")),
            OPERATORS.tokenizer(),
            Patterns.regex("\"[^\"]*\"").toScanner("string").source().map(s -> Tokens.fragment(s, "stringfragment")),
            Patterns.string("true")
                    .or(Patterns.string("false"))
                    .toScanner("boolean")
                    .source()
                    .map(s -> Tokens.fragment(s, "booleanfragment")),
            Patterns.string("null").toScanner("null").source().map(s -> Tokens.fragment(s, "nullfragment")),
            Patterns.regex("([a-zA-Z_$][\\w$\\.]*)")
                    .toScanner("variable")
                    .source()
                    .map(s -> Tokens.fragment(s, "variablefragment")),
            Patterns.regex("[0-9]+\\.[0-9]+")
                    .toScanner("strict-decimal")
                    .source()
                    .map(s -> Tokens.fragment(s, Tokens.Tag.DECIMAL)),
            Terminals.IntegerLiteral.TOKENIZER.or(Terminals.IntegerLiteral.TOKENIZER));

    private static final Parser<FeelExpression> INTEGER =
            Terminals.IntegerLiteral.PARSER.map(Integer::valueOf).map(FeelExpression.IntegerLiteral::new);

    private static final Parser<FeelExpression> DOUBLE =
            Terminals.DecimalLiteral.PARSER.map(Double::valueOf).map(FeelExpression.DoubleLiteral::new);

    private static final Parser<FeelExpression> VARIABLE =
            Terminals.fragment("variablefragment").map(FeelExpression.VariableLiteral::new);

    private static final Parser<FeelExpression> STRING = Terminals.fragment("stringfragment")
            .map(s -> s.substring(1, s.length() - 1))
            .map(FeelExpression.StringLiteral::new);

    private static final Parser<FeelExpression> BOOLEAN =
            Terminals.fragment("booleanfragment").map(Boolean::valueOf).map(FeelExpression.BooleanLiteral::new);

    private static final Parser<FeelExpression> NULL =
            Terminals.fragment("nullfragment").map(__ -> new FeelExpression.Null());

    private static final Parser<FeelExpression> DATE_TIME = Terminals.fragment("datetimefragment").map(LocalDateTime::parse).map(DateTimeLiteral::new);

    private static final Parser<FeelExpression> DATE = Terminals.fragment("datefragment").map(LocalDate::parse).map(DateLiteral::new);

    private static final Parser<FeelExpression> QUESTIONMARK = Terminals.fragment("questionmarkfragment").map(__ -> new QuestionMark());

    private static Parser<FeelExpression> parseRangeExpression(
            final Parser<Boolean> leftBound,
            final Parser<FeelExpression> expression,
            final Parser<Boolean> rightBound) {
        return Parsers.sequence(
                leftBound,
                expression,
                OPERATORS.token("..").skipTimes(1),
                expression,
                rightBound,
                (isLeftInclusive, lowerBound, __, upperBound, isRightInclusive) ->
                        new FeelExpression.RangeExpression(isLeftInclusive, lowerBound, upperBound, isRightInclusive));
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
                parseRangeExpression(op("]", false), expressionParser, op("[", false)));
    }

    private static Parser<FeelExpression> createPreAndInfixExpressionParser(
            final Parser<FeelExpression> feelExpressionParser) {
        return new OperatorTable<FeelExpression>()
                .infixr(op(",", FeelExpression.DisjunctionExpression::new), 0)
                .prefix(op("<", v -> FeelExpression.unaryExpression(Operator.LT, v)), 5)
                .prefix(op(">", v -> FeelExpression.unaryExpression(Operator.GT, v)), 5)
                .prefix(op("<=", v -> FeelExpression.unaryExpression(Operator.LE, v)), 5)
                .prefix(op(">=", v -> FeelExpression.unaryExpression(Operator.GE, v)), 5)
                .infixl(op("or", (l, r) -> FeelExpression.binaryExpression(Operator.OR, l, r)), 8)
                .infixl(op("and", (l, r) -> FeelExpression.binaryExpression(Operator.AND, l, r)), 8)
                .infixl(op("+", (l, r) -> FeelExpression.binaryExpression(Operator.ADD, l, r)), 10)
                .infixl(op("-", (l, r) -> FeelExpression.binaryExpression(Operator.SUB, l, r)), 10)
                .infixl(op("*", (l, r) -> FeelExpression.binaryExpression(Operator.MUL, l, r)), 20)
                .infixl(op("**", (l, r) -> FeelExpression.binaryExpression(Operator.EXP, l, r)), 20)
                .infixl(op("/", (l, r) -> FeelExpression.binaryExpression(Operator.DIV, l, r)), 20)
                .infixl(op(">", (l, r) -> FeelExpression.binaryExpression(Operator.GT, l, r)), 20)
                .infixl(op(">=", (l, r) -> FeelExpression.binaryExpression(Operator.GE, l, r)), 20)
                .infixl(op("<", (l, r) -> FeelExpression.binaryExpression(Operator.LT, l, r)), 20)
                .infixl(op("<=", (l, r) -> FeelExpression.binaryExpression(Operator.LE, l, r)), 20)
                .prefix(op("-", v -> FeelExpression.unaryExpression(Operator.SUB, v)), 30)
                .build(feelExpressionParser);
    }

    private static Parser<FeelExpression> parseBuiltins(
        final Parser<FeelExpression> feelParserReference
    ) {
        return Parsers.sequence(
            Terminals.fragment("builtinfragment"), OPERATORS.token("("), feelParserReference.sepBy(OPERATORS.token(",")), OPERATORS.token(")"),
            (name, __, args, ___) -> new NaryExpression(Operator.fromString(name), args)
        );
    }

    private static Parser<FeelExpression.Empty> parseEmpty() {
        return Parsers.EOF
                .map((__) -> new FeelExpression.Empty())
                .or(Terminals.fragment("emptyfragment").map((__) -> new FeelExpression.Empty()));
    }

    private static <T> Parser<T> op(final String name, final T value) {
        return OPERATORS.token(name).retn(value);
    }

    private static Parser<FeelExpression> feelExpressionParser() {
        final Parser.Reference<FeelExpression> feelParserReference = Parser.newReference();

        final Parser<FeelExpression> literalParser = Parsers.or(QUESTIONMARK, INTEGER, DOUBLE, BOOLEAN, VARIABLE, STRING, DATE_TIME, DATE);

        final Parser<FeelExpression> feelExpressionParserWithoutBinaryExpressions =
            Parsers.or(literalParser, NULL, parseBuiltins(feelParserReference.lazy()), createRangeExpressionParser(feelParserReference.lazy()));

        final Parser<FeelExpression> feelExpressionParser =
            createPreAndInfixExpressionParser(feelExpressionParserWithoutBinaryExpressions);

        feelParserReference.set(feelExpressionParser);

        return Parsers.or(parseEmpty(), feelExpressionParser);
    }

    private static boolean expressionLanguageIsFeel(final String expressionLanguage) {
        return Arrays.asList(DmnModelConstants.FEEL_NS, DmnModelConstants.FEEL12_NS, DmnModelConstants.FEEL13_NS)
                        .contains(expressionLanguage)
                || expressionLanguage.equalsIgnoreCase("feel");
    }

    static final Parser<FeelExpression> PARSER = feelExpressionParser().from(TOKENIZER, IGNORED);

    public static Either<ValidationResult.Builder.ElementStep, FeelExpression> parse(final CharSequence charSequence) {
        try {
            return new Either.Right<>(PARSER.parse(charSequence));
        } catch (final ParserException e) {
            return new Either.Left<>(
                    ValidationResult.init.message("Could not parse '" + charSequence + "': " + e.getMessage()));
        }
    }

    public static Either<ValidationResult.Builder.ElementStep, FeelExpression> parse(final Expression expression) {
        if (expressionLanguageIsFeel(expression.expressionLanguage)) {
            return parse(expression.textContent);
        } else {
            return new Either.Left<>(ValidationResult.init
                    .message("Expression language '" + expression.expressionLanguage + "' not supported")
                    .severity(Severity.WARNING));
        }
    }
}
