package de.redsix.dmncheck.feel;

import de.redsix.dmncheck.feel.model.*;
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
            IntegerLiteral::new);

    private static final Parser<FeelExpression> DOUBLE = Terminals.DecimalLiteral.PARSER.map(Double::valueOf).map(
            DoubleLiteral::new);

    private static final Parser<FeelExpression> DATE = Terminals.fragment("datefragment").map(
            string -> new DateLiteral(LocalDateTime.parse(string)));

    private static final Parser<FeelExpression> VARIABLE = Terminals.fragment("variablefragment").map(
            VariableLiteral::new);

    private static final Parser<FeelExpression> STRING = Terminals.fragment("stringfragment")
            .map(s -> s.substring(1, s.length() -1 )).map(StringLiteral::new);

    private static final Parser<FeelExpression> BOOLEAN = Terminals.fragment("booleanfragment")
            .map(Boolean::new).map(BooleanLiteral::new);

    private static Parser<Token> term(final String... names) {
        return OPERATORS.token(names);
    }

    private static <T> Parser<T> op(final String name, final T value) {
        return term(name).retn(value);
    }

    private static Parser<FeelExpression> parser() {
        final Parser.Reference<FeelExpression> reference = Parser.newReference();

        final Parser<FeelExpression> dateParser = Parsers.between(term("date and time(\""),
                DATE, term("\")"));

        final Parser<FeelExpression> boundExpression = Parsers.or(INTEGER, DOUBLE, dateParser, VARIABLE);

        final Parser<FeelExpression> rangeExpressionParser = Parsers.or(
                Parsers.sequence(term("[").retn(true), boundExpression, term("..").skipTimes(1), boundExpression, term("]").retn(true), RangeExpression::new),
                Parsers.sequence(term("]").retn(false), boundExpression, term("..").skipTimes(1), boundExpression, term("]").retn(true), RangeExpression::new),
                Parsers.sequence(term("[").retn(true), boundExpression, term("..").skipTimes(1), boundExpression, term("[").retn(false), RangeExpression::new),
                Parsers.sequence(term("(").retn(false), boundExpression, term("..").skipTimes(1), boundExpression, term("]").retn(true), RangeExpression::new),
                Parsers.sequence(term("[").retn(true), boundExpression, term("..").skipTimes(1), boundExpression, term(")").retn(false), RangeExpression::new),
                Parsers.sequence(term("(").retn(false), boundExpression, term("..").skipTimes(1), boundExpression, term(")").retn(false), RangeExpression::new),
                Parsers.sequence(term("]").retn(false), boundExpression, term("..").skipTimes(1), boundExpression, term(")").retn(false), RangeExpression::new),
                Parsers.sequence(term("(").retn(false), boundExpression, term("..").skipTimes(1), boundExpression, term("[").retn(false), RangeExpression::new),
                Parsers.sequence(term("]").retn(false), boundExpression, term("..").skipTimes(1), boundExpression, term("[").retn(false), RangeExpression::new)
                );

        final Parser<FeelExpression> parseNot = Parsers.between(term("not("), reference.lazy(), term(")"))
                .map(expression -> new UnaryExpression(Operator.NOT, expression));

        final Parser<FeelExpression> binaryExpressionParser = new OperatorTable<FeelExpression>()
                .infixr(op(",", DisjunctionExpression::new), 0)
                .prefix(op("<", v -> new UnaryExpression(Operator.LT, v)), 5)
                .prefix(op(">", v -> new UnaryExpression(Operator.GT, v)), 5)
                .prefix(op("<=", v -> new UnaryExpression(Operator.LE, v)), 5)
                .prefix(op(">=", v -> new UnaryExpression(Operator.GE, v)), 5)
                .infixl(op("or", (l, r) -> new BinaryExpression(l, Operator.OR, r)), 8)
                .infixl(op("and", (l, r) -> new BinaryExpression(l, Operator.AND, r)), 8)
                .infixl(op("+", (l, r) -> new BinaryExpression(l, Operator.ADD, r)), 10)
                .infixl(op("-", (l, r) -> new BinaryExpression(l, Operator.SUB, r)), 10)
                .infixl(op("*", (l, r) -> new BinaryExpression(l, Operator.MUL, r)), 20)
                .infixl(op("**", (l, r) -> new BinaryExpression(l, Operator.EXP, r)), 20)
                .infixl(op("/", (l, r) -> new BinaryExpression(l, Operator.DIV, r)), 20)
                .prefix(op("-", v -> new UnaryExpression(Operator.SUB, v)), 30)
                .build(Parsers.or(INTEGER, DOUBLE, BOOLEAN, VARIABLE, STRING, parseNot, rangeExpressionParser, dateParser));

        reference.set(binaryExpressionParser);

        return binaryExpressionParser;
    }

    public static final Parser<FeelExpression> PARSER = parser().from(TOKENIZER, IGNORED);
}


