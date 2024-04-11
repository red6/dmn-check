package de.redsix.dmncheck.feel;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class FeelExpressionTest {

    @Test
    void emptyNeverContainsVariable() {
        assertFalse(FeelExpressions.Empty().containsVariable("x"));
    }

    @Test
    void boolNeverContainsVariable() {
        assertFalse(FeelExpressions.BooleanLiteral(true).containsVariable("x"));
    }

    @Test
    void dateNeverContainsVariable() {
        assertFalse(FeelExpressions.DateLiteral(LocalDateTime.MIN).containsVariable("x"));
    }

    @Test
    void doubleNeverContainsVariable() {
        assertFalse(FeelExpressions.DoubleLiteral(0.0).containsVariable("x"));
    }

    @Test
    void intNeverContainsVariable() {
        assertFalse(FeelExpressions.IntegerLiteral(1).containsVariable("x"));
    }

    @Test
    void stringNeverContainsVariable() {
        assertFalse(FeelExpressions.StringLiteral("foobar").containsVariable("x"));
    }

    @Test
    void variableThatContainsVariable() {
        assertTrue(FeelExpressions.VariableLiteral("x").containsVariable("x"));
    }

    @Test
    void variableThatDoesNotContainVariable() {
        assertFalse(FeelExpressions.VariableLiteral("y").containsVariable("x"));
    }

    @Test
    void rangeContainsVariable() {
        assertTrue(FeelExpressions.RangeExpression(
                        true, FeelExpressions.VariableLiteral("x"), FeelExpressions.Empty(), false)
                .containsVariable("x"));
    }

    @Test
    void rangeDoesNotContainVariable() {
        assertFalse(FeelExpressions.RangeExpression(
                        true, FeelExpressions.VariableLiteral("y"), FeelExpressions.Empty(), false)
                .containsVariable("x"));
    }

    @Test
    void unaryContainsVariable() {
        assertTrue(FeelExpressions.UnaryExpression(Operator.GT, FeelExpressions.VariableLiteral("x"))
                .containsVariable("x"));
    }

    @Test
    void unaryDoesNotContainVariable() {
        assertFalse(FeelExpressions.UnaryExpression(Operator.GT, FeelExpressions.VariableLiteral("y"))
                .containsVariable("x"));
    }

    @Test
    void binaryContainsVariable() {
        assertTrue(FeelExpressions.BinaryExpression(
                        FeelExpressions.Empty(), Operator.GT, FeelExpressions.VariableLiteral("x"))
                .containsVariable("x"));
    }

    @Test
    void binaryDosNotContainVariable() {
        assertFalse(FeelExpressions.BinaryExpression(
                        FeelExpressions.Empty(), Operator.GT, FeelExpressions.VariableLiteral("y"))
                .containsVariable("x"));
    }

    @Test
    void disjunctionContainsVariableInTail() {
        assertTrue(FeelExpressions.DisjunctionExpression(FeelExpressions.Empty(), FeelExpressions.VariableLiteral("x"))
                .containsVariable("x"));
    }

    @Test
    void disjunctionContainsVariableInHead() {
        assertTrue(FeelExpressions.DisjunctionExpression(FeelExpressions.VariableLiteral("x"), FeelExpressions.Empty())
                .containsVariable("x"));
    }

    @Test
    void disjunctionDosNotContainVariableInTail() {
        assertFalse(FeelExpressions.DisjunctionExpression(FeelExpressions.Empty(), FeelExpressions.VariableLiteral("y"))
                .containsVariable("x"));
    }

    @Test
    void disjunctionDoesNotContainVariableInHead() {
        assertFalse(FeelExpressions.DisjunctionExpression(FeelExpressions.VariableLiteral("y"), FeelExpressions.Empty())
                .containsVariable("x"));
    }
}
