package de.redsix.dmncheck.feel;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class FeelExpressionTest {

    @Test
    void emptyNeverContainsVariable() {
        assertFalse(new FeelExpression.Empty().containsVariable("x"));
    }

    @Test
    void boolNeverContainsVariable() {
        assertFalse(
            new FeelExpression.BooleanLiteral(true).containsVariable("x")
        );
    }

    @Test
    void dateNeverContainsVariable() {
        assertFalse(
            new FeelExpression.DateLiteral(LocalDateTime.MIN).containsVariable(
                "x"
            )
        );
    }

    @Test
    void doubleNeverContainsVariable() {
        assertFalse(
            new FeelExpression.DoubleLiteral(0.0).containsVariable("x")
        );
    }

    @Test
    void intNeverContainsVariable() {
        assertFalse(new FeelExpression.IntegerLiteral(1).containsVariable("x"));
    }

    @Test
    void stringNeverContainsVariable() {
        assertFalse(
            new FeelExpression.StringLiteral("foobar").containsVariable("x")
        );
    }

    @Test
    void variableThatContainsVariable() {
        assertTrue(
            new FeelExpression.VariableLiteral("x").containsVariable("x")
        );
    }

    @Test
    void variableThatDoesNotContainVariable() {
        assertFalse(
            new FeelExpression.VariableLiteral("y").containsVariable("x")
        );
    }

    @Test
    void rangeContainsVariable() {
        assertTrue(
            new FeelExpression.RangeExpression(
                true,
                new FeelExpression.VariableLiteral("x"),
                new FeelExpression.Empty(),
                false
            ).containsVariable("x")
        );
    }

    @Test
    void rangeDoesNotContainVariable() {
        assertFalse(
            new FeelExpression.RangeExpression(
                true,
                new FeelExpression.VariableLiteral("y"),
                new FeelExpression.Empty(),
                false
            ).containsVariable("x")
        );
    }

    @Test
    void unaryContainsVariable() {
        assertTrue(
            new FeelExpression.UnaryExpression(
                Operator.GT,
                new FeelExpression.VariableLiteral("x")
            ).containsVariable("x")
        );
    }

    @Test
    void unaryDoesNotContainVariable() {
        assertFalse(
            new FeelExpression.UnaryExpression(
                Operator.GT,
                new FeelExpression.VariableLiteral("y")
            ).containsVariable("x")
        );
    }

    @Test
    void binaryContainsVariable() {
        assertTrue(
            new FeelExpression.BinaryExpression(
                new FeelExpression.Empty(),
                Operator.GT,
                new FeelExpression.VariableLiteral("x")
            ).containsVariable("x")
        );
    }

    @Test
    void binaryDosNotContainVariable() {
        assertFalse(
            new FeelExpression.BinaryExpression(
                new FeelExpression.Empty(),
                Operator.GT,
                new FeelExpression.VariableLiteral("y")
            ).containsVariable("x")
        );
    }

    @Test
    void disjunctionContainsVariableInTail() {
        assertTrue(
            new FeelExpression.DisjunctionExpression(
                new FeelExpression.Empty(),
                new FeelExpression.VariableLiteral("x")
            ).containsVariable("x")
        );
    }

    @Test
    void disjunctionContainsVariableInHead() {
        assertTrue(
            new FeelExpression.DisjunctionExpression(
                new FeelExpression.VariableLiteral("x"),
                new FeelExpression.Empty()
            ).containsVariable("x")
        );
    }

    @Test
    void disjunctionDosNotContainVariableInTail() {
        assertFalse(
            new FeelExpression.DisjunctionExpression(
                new FeelExpression.Empty(),
                new FeelExpression.VariableLiteral("y")
            ).containsVariable("x")
        );
    }

    @Test
    void disjunctionDoesNotContainVariableInHead() {
        assertFalse(
            new FeelExpression.DisjunctionExpression(
                new FeelExpression.VariableLiteral("y"),
                new FeelExpression.Empty()
            ).containsVariable("x")
        );
    }
}
