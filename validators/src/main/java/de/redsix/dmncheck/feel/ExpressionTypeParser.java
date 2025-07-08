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

    private static final Parser<Void> ignored = Scanners.WHITESPACES.skipMany();

    private static Parser<?> tokenizer(
        Collection<ItemDefinition> itemDefinitions
    ) {
        final Parser<?> itemDefinitionTokenizer = itemDefinitions
            .stream()
            .map(NamedElement::getName)
            .filter(Objects::nonNull)
            .map(name ->
                Patterns.string(name)
                    .toScanner("itemDefinition")
                    .source()
                    .map(s -> Tokens.fragment(s, "itemDefinitionFragment"))
            )
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
                .map(s -> Tokens.fragment(s, "enumfragment"))
        );
    }

    private static final Parser<ExpressionType> stringParser =
        Terminals.fragment("stringfragment").map(__ ->
            new ExpressionType.String()
        );
    private static final Parser<ExpressionType> booleanParser =
        Terminals.fragment("booleanfragment").map(__ ->
            new ExpressionType.Boolean()
        );
    private static final Parser<ExpressionType> integerParser =
        Terminals.fragment("integerfragment").map(__ ->
            new ExpressionType.Integer()
        );
    private static final Parser<ExpressionType> longParser = Terminals.fragment(
        "longfragment"
    ).map(__ -> new ExpressionType.Long());
    private static final Parser<ExpressionType> doubleParser =
        Terminals.fragment("doublefragment").map(__ ->
            new ExpressionType.Double()
        );
    private static final Parser<ExpressionType> dateParser = Terminals.fragment(
        "datefragment"
    ).map(__ -> new ExpressionType.Date());
    private static final Parser<ExpressionType> enumParser = Terminals.fragment(
        "enumfragment"
    ).map(ExpressionType.Enum::new);
    private static final Parser<ExpressionType> topParser = Parsers.EOF.map(
        __ -> new ExpressionType.Top()
    );

    private static Parser<ExpressionType> itemDefinitionParser(
        Collection<ItemDefinition> itemDefinitions
    ) {
        return Terminals.fragment("itemDefinitionFragment").map(name -> {
                final ItemDefinition matchedItemDefinition = itemDefinitions
                    .stream()
                    .filter(itemDefinition ->
                        name.equals(itemDefinition.getName())
                    )
                    .findFirst()
                    .orElseThrow(() ->
                        new IllegalStateException("ItemDefinitions are broken.")
                    );
                return new ExpressionType.ItemDefintion(matchedItemDefinition);
            });
    }

    static Parser<ExpressionType> parser(
        Collection<ItemDefinition> itemDefinitions
    ) {
        return Parsers.or(
            stringParser,
            booleanParser,
            integerParser,
            longParser,
            doubleParser,
            dateParser,
            itemDefinitionParser(itemDefinitions),
            enumParser,
            topParser
        ).from(tokenizer(itemDefinitions), ignored);
    }

    public static Either<
        ValidationResult.Builder.ElementStep,
        ExpressionType
    > parse(
        final CharSequence charSequence,
        Collection<ItemDefinition> itemDefinitions
    ) {
        try {
            return new Either.Right<>(
                charSequence != null
                    ? parser(itemDefinitions).parse(charSequence)
                    : new ExpressionType.Top()
            );
        } catch (final ParserException e) {
            return new Either.Left<>(
                ValidationResult.init.message(
                    "Could not parse FEEL expression type '" +
                    charSequence +
                    "'"
                )
            );
        }
    }
}
