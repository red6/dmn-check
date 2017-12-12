package de.redsix.dmncheck.feel;

import org.jparsec.*;
import org.jparsec.pattern.Patterns;

import java.time.LocalDateTime;

public class FeelParser {

    private static final Terminals OPERATORS = Terminals
            .operators("+", "-", "*", "**", "/", "(", ")", "[", "]", "..", ",", "not(", "and", "or", "<", ">", "<=", ">=",
                    "date and time(\"", "\")");

    private static final Parser<Void> IGNORED = Scanners.WHITESPACES.skipMany();

    private static final Parser<?> TOKENIZER = Parsers.or(
            Patterns.regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}").toScanner("date").source().map(s ->
                    Tokens.fragment(s, "datefragment")),
            Patterns.regex("\"[\\w]+\"").toScanner("string").source().map(s ->
                    Tokens.fragment(s, "stringfragment")),
            OPERATORS.tokenizer(),
            Patterns.string("true").or(Patterns.string("false")).toScanner("boolean").source().map(s ->
                    Tokens.fragment(s, "booleanfragment")),
            Patterns.regex("([a-zA-Z_$][\\w$\\.]*)").toScanner("variable").source().map(s ->
                    Tokens.fragment(s, "variablefragment")),
            Patterns.regex("[0-9]+\\.[0-9]+").toScanner("strict-decimal").source().map(s ->
                    Tokens.fragment(s, Tokens.Tag.DECIMAL)),
            Terminals.IntegerLiteral.TOKENIZER.or(Terminals.IntegerLiteral.TOKENIZER)
    );

    private static final Parser<FeelExpression> INTEGER = Terminals.IntegerLiteral.PARSER.map(Integer::valueOf).map(
            FeelExpressions::IntegerLiteral);

    private static final Parser<FeelExpression> DOUBLE = Terminals.DecimalLiteral.PARSER.map(Double::valueOf).map(
            FeelExpressions::DoubleLiteral);

    private static final Parser<FeelExpression> DATE = Terminals.fragment("datefragment")
            .map(LocalDateTime::parse).map(FeelExpressions::DateLiteral);

    private static final Parser<FeelExpression> VARIABLE = Terminals.fragment("variablefragment").map(
            FeelExpressions::VariableLiteral);

    private static final Parser<FeelExpression> STRING = Terminals.fragment("stringfragment")
            .map(s -> s.substring(1, s.length() -1 )).map(FeelExpressions::StringLiteral);

    private static final Parser<FeelExpression> BOOLEAN = Terminals.fragment("booleanfragment")
            .map(Boolean::new).map(FeelExpressions::BooleanLiteral);

    private static final Parser<FeelExpression> dateParser = Parsers.between(term("date and time(\""),
            DATE, term("\")"));

    private static final Parser<FeelExpression> boundExpression = Parsers.or(INTEGER, DOUBLE, dateParser, VARIABLE);


    private static Parser<FeelExpression> parseRangeExpression(final Parser<Boolean> leftBound, final Parser<Boolean> rightBound) {
        return Parsers.sequence(leftBound, boundExpression, term("..").skipTimes(1), boundExpression, rightBound,
                (isLeftInclusive, lowerBound, aVoid, upperBound, isRightInclusive) -> FeelExpressions.RangeExpression(
                        isLeftInclusive, lowerBound, upperBound, isRightInclusive));
    }

    private static Parser<Token> term(final String... names) {
        return OPERATORS.token(names);
    }

    private static <T> Parser<T> op(final String name, final T value) {
        return term(name).retn(value);
    }

    private static Parser<FeelExpression> parser() {
        final Parser.Reference<FeelExpression> reference = Parser.newReference();

        final Parser<FeelExpression> rangeExpressionParser = Parsers.or(
                parseRangeExpression(term("[").retn(true), term("]").retn(true)),
                parseRangeExpression(term("]").retn(false),term("]").retn(true)),
                parseRangeExpression(term("[").retn(true), term("[").retn(false)),
                parseRangeExpression(term("(").retn(false), term("]").retn(true)),
                parseRangeExpression(term("[").retn(true), term(")").retn(false)),
                parseRangeExpression(term("(").retn(false), term(")").retn(false)),
                parseRangeExpression(term("]").retn(false), term(")").retn(false)),
                parseRangeExpression(term("(").retn(false), term("[").retn(false)),
                parseRangeExpression(term("]").retn(false), term("[").retn(false))
                );

        final Parser<FeelExpression> parseNot = Parsers.between(term("not("), reference.lazy(), term(")"))
                .map(expression -> FeelExpressions.UnaryExpression(Operator.NOT, expression));

        final Parser<FeelExpression> binaryExpressionParser = new OperatorTable<FeelExpression>()
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
                .build(Parsers.or(INTEGER, DOUBLE, BOOLEAN, VARIABLE, STRING, parseNot, rangeExpressionParser, dateParser));

        reference.set(binaryExpressionParser);

        return binaryExpressionParser;
    }

    public static final Parser<FeelExpression> PARSER = parser().from(TOKENIZER, IGNORED);
}


