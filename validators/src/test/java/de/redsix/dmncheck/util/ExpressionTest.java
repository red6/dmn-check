package de.redsix.dmncheck.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.camunda.bpm.model.dmn.impl.DmnModelConstants;
import org.camunda.bpm.model.dmn.instance.LiteralExpression;
import org.camunda.bpm.model.dmn.instance.UnaryTests;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ExpressionTest {

    @Test
    void shouldCreateExpressionFromUnaryTestsWithLocalExpressionLanguage() {
        final UnaryTests unaryTests = Mockito.mock(UnaryTests.class);
        when(unaryTests.getExpressionLanguage()).thenReturn(DmnModelConstants.FEEL_NS);
        when(unaryTests.getTextContent()).thenReturn("");

        final Expression expression = new Expression(unaryTests, null);

        assertEquals(DmnModelConstants.FEEL_NS, expression.expressionLanguage);
        assertEquals("", expression.textContent);
    }

    @Test
    void shouldCreateExpressionFromLiteralExpressionWithLocalExpressionLanguage() {
        final LiteralExpression literalExpression = Mockito.mock(LiteralExpression.class);
        when(literalExpression.getExpressionLanguage()).thenReturn(DmnModelConstants.FEEL_NS);
        when(literalExpression.getTextContent()).thenReturn("");

        final Expression expression = new Expression(literalExpression, null);

        assertEquals(DmnModelConstants.FEEL_NS, expression.expressionLanguage);
        assertEquals("", expression.textContent);
    }

    @Test
    void shouldFavorLocalExpressionLanguageUnaryTests() {
        final UnaryTests unaryTests = Mockito.mock(UnaryTests.class);
        when(unaryTests.getExpressionLanguage()).thenReturn(DmnModelConstants.FEEL_NS);
        when(unaryTests.getTextContent()).thenReturn("");

        final Expression expression = new Expression(unaryTests, "javascript");

        assertEquals(DmnModelConstants.FEEL_NS, expression.expressionLanguage);
        assertEquals("", expression.textContent);
    }

    @Test
    void shouldFavorLocalExpressionLanguageLiteralExpression() {
        final LiteralExpression literalExpression = Mockito.mock(LiteralExpression.class);
        when(literalExpression.getExpressionLanguage()).thenReturn(DmnModelConstants.FEEL_NS);
        when(literalExpression.getTextContent()).thenReturn("");

        final Expression expression = new Expression(literalExpression, "javascript");

        assertEquals(DmnModelConstants.FEEL_NS, expression.expressionLanguage);
        assertEquals("", expression.textContent);
    }

    @Test
    void shouldUseTopLevelIfLocalExpressionLanguageIsMissingUnaryTests() {
        final UnaryTests unaryTests = Mockito.mock(UnaryTests.class);
        when(unaryTests.getTextContent()).thenReturn("");

        final Expression expression = new Expression(unaryTests, "javascript");

        assertEquals("javascript", expression.expressionLanguage);
        assertEquals("", expression.textContent);
    }

    @Test
    void shouldUseTopLevelIfLocalExpressionLanguageIsMissingLiteralExpression() {
        final LiteralExpression literalExpression = Mockito.mock(LiteralExpression.class);
        when(literalExpression.getTextContent()).thenReturn("");

        final Expression expression = new Expression(literalExpression, "javascript");

        assertEquals("javascript", expression.expressionLanguage);
        assertEquals("", expression.textContent);
    }

    @Test
    void shouldUseFeelIfNothingIsSpecifiedUnaryTests() {
        final UnaryTests unaryTests = Mockito.mock(UnaryTests.class);
        when(unaryTests.getTextContent()).thenReturn("");

        final Expression expression = new Expression(unaryTests, null);

        assertEquals(DmnModelConstants.FEEL_NS, expression.expressionLanguage);
        assertEquals("", expression.textContent);
    }

    @Test
    void shouldUseFeelIfNothingIsSpecifiedLiteralExpression() {
        final LiteralExpression literalExpression = Mockito.mock(LiteralExpression.class);
        when(literalExpression.getTextContent()).thenReturn("");

        final Expression expression = new Expression(literalExpression, null);

        assertEquals(DmnModelConstants.FEEL_NS, expression.expressionLanguage);
        assertEquals("", expression.textContent);
    }
}
