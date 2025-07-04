package de.redsix.dmncheck.feel;

import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.util.Either;

import java.util.Collection;
import java.util.Objects;
import org.camunda.bpm.model.dmn.instance.ItemDefinition;
import org.camunda.bpm.model.dmn.instance.NamedElement;
import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.jparsec.Scanners;
import org.jparsec.Terminals;
import org.jparsec.Tokens;
import org.jparsec.error.ParserException;
import org.jparsec.pattern.Patterns;

public final class ExpressionTypeParser {

    private ExpressionTypeParser() {}

    private static final Parser<Void> IGNORED = Scanners.WHITESPACES.skipMany();

    private static Parser<?> TOKENIZER(Collection<ItemDefinition> itemDefinitions) {
        final Parser<?> itemDefinitionTokenizer = itemDefinitions.stream()
                .map(NamedElement::getName)
                .filter(Objects::nonNull)
                .map(name -> Patterns.string(name)
                        .toScanner("itemDefinition")
                        .source()
                        .map(s -> Tokens.fragment(s, "itemDefinitionFragment")))
                .reduce(Parsers.never(), Parsers::or);

        return Parsers.or(
                Patterns.stringCaseInsensitive("string")
                        .toScanner("string")
                        .source()
                        .map(s -> Tokens.fragment(s, "stringfragment")),
                Patterns.stringCaseInsensitive("boolean")
                        .toScanner("boolean")
                        .source()
                        .map(s -> Tokens.fragment(s, "booleanfragment")),
                Patterns.stringCaseInsensitive("integer")
                        .toScanner("boolean")
                        .source()
                        .map(s -> Tokens.fragment(s, "integerfragment")),
                Patterns.stringCaseInsensitive("long")
                        .toScanner("boolean")
                        .source()
                        .map(s -> Tokens.fragment(s, "longfragment")),
                Patterns.stringCaseInsensitive("double")
                        .toScanner("boolean")
                        .source()
                        .map(s -> Tokens.fragment(s, "doublefragment")),
                Patterns.stringCaseInsensitive("date")
                        .toScanner("boolean")
                        .source()
                        .map(s -> Tokens.fragment(s, "datefragment")),
                itemDefinitionTokenizer,
                Patterns.regex("([a-z][a-z_0-9]*\\.)*[A-Z_]($[A-Z_]|[\\w_])*")
                        .toScanner("enum")
                        .source()
                        .map(s -> Tokens.fragment(s, "enumfragment")));
    }

    private static final Parser<ExpressionType> STRING =
            Terminals.fragment("stringfragment").map(__ -> new ExpressionType.STRING());
    private static final Parser<ExpressionType> BOOLEAN =
            Terminals.fragment("booleanfragment").map(__ -> new ExpressionType.BOOLEAN());
    private static final Parser<ExpressionType> INTEGER =
            Terminals.fragment("integerfragment").map(__ -> new ExpressionType.INTEGER());
    private static final Parser<ExpressionType> LONG =
            Terminals.fragment("longfragment").map(__ -> new ExpressionType.LONG());
    private static final Parser<ExpressionType> DOUBLE =
            Terminals.fragment("doublefragment").map(__ -> new ExpressionType.DOUBLE());
    private static final Parser<ExpressionType> DATE =
            Terminals.fragment("datefragment").map(__ -> new ExpressionType.DATE());
    private static final Parser<ExpressionType> ENUM =
            Terminals.fragment("enumfragment").map(ExpressionType.ENUM::new);
    private static final Parser<ExpressionType> TOP = Parsers.EOF.map((__) -> new ExpressionType.TOP());

    private static Parser<ExpressionType> ITEMDEFINITION(Collection<ItemDefinition> itemDefinitions) {
        return Terminals.fragment("itemDefinitionFragment").map(name -> {
            final ItemDefinition matchedItemDefinition = itemDefinitions.stream()
                    .filter(itemDefinition -> name.equals(itemDefinition.getName()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("ItemDefinitions are broken."));
            return new ExpressionType.ITEMDEFINITION(matchedItemDefinition);
        });
    }

    static Parser<ExpressionType> PARSER(Collection<ItemDefinition> itemDefinitions) {
        return Parsers.or(STRING, BOOLEAN, INTEGER, LONG, DOUBLE, DATE, ITEMDEFINITION(itemDefinitions), ENUM, TOP)
                .from(TOKENIZER(itemDefinitions), IGNORED);
    }

    public static Either<ValidationResult.Builder.ElementStep, ExpressionType> parse(
            final CharSequence charSequence, Collection<ItemDefinition> itemDefinitions) {
        try {
            return new Either.Right<>(
                    charSequence != null ? PARSER(itemDefinitions).parse(charSequence) : new ExpressionType.TOP());
        } catch (final ParserException e) {
            return new Either.Left<>(
                    ValidationResult.init.message("Could not parse FEEL expression type '" + charSequence + "'"));
        }
    }
}
