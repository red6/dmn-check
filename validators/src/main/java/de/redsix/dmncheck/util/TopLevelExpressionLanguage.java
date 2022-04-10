package de.redsix.dmncheck.util;

import org.camunda.bpm.model.dmn.instance.LiteralExpression;
import org.camunda.bpm.model.dmn.instance.UnaryTests;
import org.checkerframework.checker.nullness.qual.Nullable;

public class TopLevelExpressionLanguage {

    @Nullable
    public final String topLevelExpressionLanguage;

    public TopLevelExpressionLanguage(final @Nullable String topLevelExpressionLanguage) {
        this.topLevelExpressionLanguage = topLevelExpressionLanguage;
    }

    public Expression toExpression(final UnaryTests unaryTests) {
        return new Expression(unaryTests, topLevelExpressionLanguage);
    }

    public Expression toExpression(final LiteralExpression literalExpression) {
        return new Expression(literalExpression, topLevelExpressionLanguage);
    }
}
