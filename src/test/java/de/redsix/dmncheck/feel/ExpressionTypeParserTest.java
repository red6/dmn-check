package de.redsix.dmncheck.feel;

import de.redsix.dmncheck.validators.util.TestEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExpressionTypeParserTest {

    @Test
    void shouldParseEmpty() {
        final ExpressionType type = ExpressionTypeParser.PARSER.parse("");

        final ExpressionType expectedType = ExpressionTypes.TOP();

        assertEquals(expectedType, type);
    }

    @Test
    void shouldParseBool() {
        final ExpressionType type = ExpressionTypeParser.PARSER.parse("boolean");

        final ExpressionType expectedType = ExpressionTypes.BOOLEAN();

        assertEquals(expectedType, type);
    }

    @Test
    void shouldParseInteger() {
        final ExpressionType type = ExpressionTypeParser.PARSER.parse("integer");

        final ExpressionType expectedType = ExpressionTypes.INTEGER();

        assertEquals(expectedType, type);
    }

    @Test
    void shouldParseString() {
        final ExpressionType type = ExpressionTypeParser.PARSER.parse("string");

        final ExpressionType expectedType = ExpressionTypes.STRING();

        assertEquals(expectedType, type);
    }

    @Test
    void shouldParseDouble() {
        final ExpressionType type = ExpressionTypeParser.PARSER.parse("double");

        final ExpressionType expectedType = ExpressionTypes.DOUBLE();

        assertEquals(expectedType, type);
    }

    @Test
    void shouldParseDate() {
        final ExpressionType type = ExpressionTypeParser.PARSER.parse("date");

        final ExpressionType expectedType = ExpressionTypes.DATE();

        assertEquals(expectedType, type);
    }

    @Test
    void shouldParseEnum() {
        final ExpressionType type = ExpressionTypeParser.PARSER.parse(TestEnum.class.getCanonicalName());

        final ExpressionType expectedType = ExpressionTypes.ENUM(TestEnum.class.getCanonicalName());

        assertEquals(expectedType, type);
    }
}
