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

        final ExpressionType expectedType = ExpressionTypes.TOP();

        assertEquals(expectedType, type);
    }

    @Test
    void shouldParseBool() {
        final List<ItemDefinition> itemDefinitions = Collections.emptyList();

        final ExpressionType type = ExpressionTypeParser.PARSER(itemDefinitions).parse("boolean");

        final ExpressionType expectedType = ExpressionTypes.BOOLEAN();

        assertEquals(expectedType, type);
    }

    @Test
    void shouldParseInteger() {
        final List<ItemDefinition> itemDefinitions = Collections.emptyList();

        final ExpressionType type = ExpressionTypeParser.PARSER(itemDefinitions).parse("integer");

        final ExpressionType expectedType = ExpressionTypes.INTEGER();

        assertEquals(expectedType, type);
    }

    @Test
    void shouldParseString() {
        final List<ItemDefinition> itemDefinitions = Collections.emptyList();

        final ExpressionType type = ExpressionTypeParser.PARSER(itemDefinitions).parse("string");

        final ExpressionType expectedType = ExpressionTypes.STRING();

        assertEquals(expectedType, type);
    }

    @Test
    void shouldParseDouble() {
        final List<ItemDefinition> itemDefinitions = Collections.emptyList();

        final ExpressionType type = ExpressionTypeParser.PARSER(itemDefinitions).parse("double");

        final ExpressionType expectedType = ExpressionTypes.DOUBLE();

        assertEquals(expectedType, type);
    }

    @Test
    void shouldParseDate() {
        final List<ItemDefinition> itemDefinitions = Collections.emptyList();

        final ExpressionType type = ExpressionTypeParser.PARSER(itemDefinitions).parse("date");

        final ExpressionType expectedType = ExpressionTypes.DATE();

        assertEquals(expectedType, type);
    }

    @Test
    void shouldParseEnum() {
        final List<ItemDefinition> itemDefinitions = Collections.emptyList();

        final ExpressionType type =
                ExpressionTypeParser.PARSER(itemDefinitions).parse(TestEnum.class.getCanonicalName());

        final ExpressionType expectedType = ExpressionTypes.ENUM(TestEnum.class.getCanonicalName());

        assertEquals(expectedType, type);
    }

    @Test
    void shouldParseItemDefinition() {
        final ItemDefinition itemDefinition = Dmn.createEmptyModel().newInstance(ItemDefinition.class);
        itemDefinition.setName("myItemDefinition");

        final List<ItemDefinition> itemDefinitions = Collections.singletonList(itemDefinition);

        final ExpressionType type = ExpressionTypeParser.PARSER(itemDefinitions).parse("myItemDefinition");

        final ExpressionType expectedType = ExpressionTypes.ITEMDEFINITION(itemDefinition);

        assertEquals(expectedType, type);
    }
}
