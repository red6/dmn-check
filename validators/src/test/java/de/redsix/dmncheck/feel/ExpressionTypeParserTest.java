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

        final ExpressionType type = ExpressionTypeParser.parser(
            itemDefinitions
        ).parse("");

        final ExpressionType expectedType = new ExpressionType.Top();

        assertEquals(expectedType, type);
    }

    @Test
    void shouldParseBool() {
        final List<ItemDefinition> itemDefinitions = Collections.emptyList();

        final ExpressionType type = ExpressionTypeParser.parser(
            itemDefinitions
        ).parse("boolean");

        final ExpressionType expectedType = new ExpressionType.Boolean();

        assertEquals(expectedType, type);
    }

    @Test
    void shouldParseInteger() {
        final List<ItemDefinition> itemDefinitions = Collections.emptyList();

        final ExpressionType type = ExpressionTypeParser.parser(
            itemDefinitions
        ).parse("integer");

        final ExpressionType expectedType = new ExpressionType.Integer();

        assertEquals(expectedType, type);
    }

    @Test
    void shouldParseString() {
        final List<ItemDefinition> itemDefinitions = Collections.emptyList();

        final ExpressionType type = ExpressionTypeParser.parser(
            itemDefinitions
        ).parse("string");

        final ExpressionType expectedType = new ExpressionType.String();

        assertEquals(expectedType, type);
    }

    @Test
    void shouldParseDouble() {
        final List<ItemDefinition> itemDefinitions = Collections.emptyList();

        final ExpressionType type = ExpressionTypeParser.parser(
            itemDefinitions
        ).parse("double");

        final ExpressionType expectedType = new ExpressionType.Double();

        assertEquals(expectedType, type);
    }

    @Test
    void shouldParseDate() {
        final List<ItemDefinition> itemDefinitions = Collections.emptyList();

        final ExpressionType type = ExpressionTypeParser.parser(
            itemDefinitions
        ).parse("date");

        final ExpressionType expectedType = new ExpressionType.Date();

        assertEquals(expectedType, type);
    }

    @Test
    void shouldParseEnum() {
        final List<ItemDefinition> itemDefinitions = Collections.emptyList();

        final ExpressionType type = ExpressionTypeParser.parser(
            itemDefinitions
        ).parse(TestEnum.class.getCanonicalName());

        final ExpressionType expectedType = new ExpressionType.Enum(
            TestEnum.class.getCanonicalName()
        );

        assertEquals(expectedType, type);
    }

    @Test
    void shouldParseItemDefinition() {
        final ItemDefinition itemDefinition =
            Dmn.createEmptyModel().newInstance(ItemDefinition.class);
        itemDefinition.setName("myItemDefinition");

        final List<ItemDefinition> itemDefinitions = Collections.singletonList(
            itemDefinition
        );

        final ExpressionType type = ExpressionTypeParser.parser(
            itemDefinitions
        ).parse("myItemDefinition");

        final ExpressionType expectedType = new ExpressionType.ItemDefintion(
            itemDefinition
        );

        assertEquals(expectedType, type);
    }
}
