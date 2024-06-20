package de.redsix.dmncheck.util;

import java.util.Objects;
import org.camunda.bpm.model.dmn.impl.DmnModelConstants;
import org.camunda.bpm.model.dmn.instance.LiteralExpression;
import org.camunda.bpm.model.dmn.instance.UnaryTests;
import org.checkerframework.checker.nullness.qual.Nullable;

public class Expression {

    public final String textContent;
    public final String expressionLanguage;

    public Expression(final UnaryTests unaryTests, final @Nullable String toplevelExpressionLanguage) {
        this.textContent = unaryTests.getTextContent();
        this.expressionLanguage =
                decideExpressionLanguage(unaryTests.getExpressionLanguage(), toplevelExpressionLanguage);
    }

    public Expression(final LiteralExpression literalExpression, final @Nullable String toplevelExpressionLanguage) {
        this.textContent = literalExpression.getTextContent();
        this.expressionLanguage =
                decideExpressionLanguage(literalExpression.getExpressionLanguage(), toplevelExpressionLanguage);
    }

    private static String decideExpressionLanguage(
            final String localExpressionLanguage, final @Nullable String toplevelExpressionLanguage) {
        return Objects.requireNonNullElseGet(
                localExpressionLanguage,
                () -> Objects.requireNonNullElse(toplevelExpressionLanguage, DmnModelConstants.FEEL_NS));
    }
}
