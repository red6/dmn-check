package de.redsix.dmncheck.feel;

import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.util.Either;
import de.redsix.dmncheck.util.Eithers;
import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.jparsec.Scanners;
import org.jparsec.Terminals;
import org.jparsec.Tokens;
import org.jparsec.error.ParserException;
import org.jparsec.pattern.Patterns;

public final class ExpressionTypeParser {

    private static final Parser<Void> IGNORED = Scanners.WHITESPACES.skipMany();

    private static final Parser<?> TOKENIZER = Parsers.or(
            Patterns.stringCaseInsensitive("string").toScanner("string").source().map(s -> Tokens.fragment(s, "stringfragment")),
            Patterns.stringCaseInsensitive("boolean").toScanner("boolean").source().map(s -> Tokens.fragment(s, "booleanfragment")),
            Patterns.stringCaseInsensitive("integer").toScanner("boolean").source().map(s -> Tokens.fragment(s, "integerfragment")),
            Patterns.stringCaseInsensitive("long").toScanner("boolean").source().map(s -> Tokens.fragment(s, "longfragment")),
            Patterns.stringCaseInsensitive("double").toScanner("boolean").source().map(s -> Tokens.fragment(s, "doublefragment")),
            Patterns.stringCaseInsensitive("date").toScanner("boolean").source().map(s -> Tokens.fragment(s, "datefragment")),
            Patterns.regex("([a-z][a-z_0-9]*\\.)*[A-Z_]($[A-Z_]|[\\w_])*").toScanner("enum").source()
                    .map(s -> Tokens.fragment(s, "enumfragment"))
            );

    private static final Parser<ExpressionType> STRING = Terminals.fragment("stringfragment").map(__ -> ExpressionTypes.STRING());
    private static final Parser<ExpressionType> BOOLEAN = Terminals.fragment("booleanfragment").map(__ -> ExpressionTypes.BOOLEAN());
    private static final Parser<ExpressionType> INTEGER = Terminals.fragment("integerfragment").map(__ -> ExpressionTypes.INTEGER());
    private static final Parser<ExpressionType> LONG = Terminals.fragment("longfragment").map(__ -> ExpressionTypes.LONG());
    private static final Parser<ExpressionType> DOUBLE = Terminals.fragment("doublefragment").map(__ -> ExpressionTypes.DOUBLE());
    private static final Parser<ExpressionType> DATE = Terminals.fragment("datefragment").map(__ -> ExpressionTypes.DATE());
    private static final Parser<ExpressionType> ENUM = Terminals.fragment("enumfragment").map(ExpressionTypes::ENUM);
    private static final Parser<ExpressionType> TOP = Parsers.EOF.map((__) -> ExpressionTypes.TOP());

    static final Parser<ExpressionType> PARSER = Parsers.or(STRING, BOOLEAN, INTEGER, LONG, DOUBLE, DATE, ENUM, TOP).from(TOKENIZER, IGNORED);

    public static Either<ValidationResult.Builder.ElementStep, ExpressionType> parse(final CharSequence charSequence) {
        try {
            return Eithers.right(charSequence != null ? PARSER.parse(charSequence) : ExpressionTypes.TOP());
        } catch (final ParserException e) {
            return Eithers.left(ValidationResult.init.message("Could not parse FEEL expression type '" + charSequence + "'"));
        }
    }
}
