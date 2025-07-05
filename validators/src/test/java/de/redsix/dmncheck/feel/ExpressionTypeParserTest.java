package de.redsix.dmncheck.feel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.redsix.dmncheck.validators.util.TestEnum;
import java.util.Collections;
import java.util.List;
import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.instance.ItemDefinition;
import org.junit.jupiter.api.Test;

class ExpressionTypeParserTest {

    @Test
    void shouldParseEmpty() {
        final List<ItemDefinition> itemDefinitions = Collections.emptyList();

        final ExpressionType type = ExpressionTypeParser.PARSER(itemDefinitions).parse("");

        final ExpressionType expectedType = new ExpressionType.TOP();

        assertEquals(expectedType, type);
    }

    @Test
    void shouldParseBool() {
        final List<ItemDefinition> itemDefinitions = Collections.emptyList();

        final ExpressionType type = ExpressionTypeParser.PARSER(itemDefinitions).parse("boolean");

        final ExpressionType expectedType = new ExpressionType.BOOLEAN();

        assertEquals(expectedType, type);
    }

    @Test
    void shouldParseInteger() {
        final List<ItemDefinition> itemDefinitions = Collections.emptyList();

        final ExpressionType type = ExpressionTypeParser.PARSER(itemDefinitions).parse("integer");

        final ExpressionType expectedType = new ExpressionType.INTEGER();

        assertEquals(expectedType, type);
    }

    @Test
    void shouldParseString() {
        final List<ItemDefinition> itemDefinitions = Collections.emptyList();

        final ExpressionType type = ExpressionTypeParser.PARSER(itemDefinitions).parse("string");

        final ExpressionType expectedType = new ExpressionType.STRING();

        assertEquals(expectedType, type);
    }

    @Test
    void shouldParseDouble() {
        final List<ItemDefinition> itemDefinitions = Collections.emptyList();

        final ExpressionType type = ExpressionTypeParser.PARSER(itemDefinitions).parse("double");

        final ExpressionType expectedType = new ExpressionType.DOUBLE();

        assertEquals(expectedType, type);
    }

    @Test
    void shouldParseDate() {
        final List<ItemDefinition> itemDefinitions = Collections.emptyList();

        final ExpressionType type = ExpressionTypeParser.PARSER(itemDefinitions).parse("date");

        final ExpressionType expectedType = new ExpressionType.DATE();

        assertEquals(expectedType, type);
    }

    @Test
    void shouldParseEnum() {
        final List<ItemDefinition> itemDefinitions = Collections.emptyList();

        final ExpressionType type =
                ExpressionTypeParser.PARSER(itemDefinitions).parse(TestEnum.class.getCanonicalName());

        final ExpressionType expectedType = new ExpressionType.ENUM(TestEnum.class.getCanonicalName());

        assertEquals(expectedType, type);
    }

    @Test
    void shouldParseItemDefinition() {
        final ItemDefinition itemDefinition = Dmn.createEmptyModel().newInstance(ItemDefinition.class);
        itemDefinition.setName("myItemDefinition");

        final List<ItemDefinition> itemDefinitions = Collections.singletonList(itemDefinition);

        final ExpressionType type = ExpressionTypeParser.PARSER(itemDefinitions).parse("myItemDefinition");

        final ExpressionType expectedType = new ExpressionType.ITEMDEFINITION(itemDefinition);

        assertEquals(expectedType, type);
    }
}
