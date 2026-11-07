package de.redsix.dmncheck.validators;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.util.WithDefinitions;
import java.util.List;
import org.camunda.bpm.model.dmn.instance.Decision;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.LiteralExpression;
import org.camunda.bpm.model.dmn.instance.Variable;
import org.junit.jupiter.api.Test;

class LiteralExpressionValidatorTest extends WithDefinitions {

    private final LiteralExpressionValidator testee = new LiteralExpressionValidator();

    @Test
    void literalExpressionWithNamedVariableIsValid() {
        final Decision decision = addDecisionWithLiteralExpression();
        final Variable variable = modelInstance.newInstance(Variable.class);
        variable.setName("season");
        decision.setVariable(variable);

        assertEquals(List.of(), testee.apply(modelInstance));
    }

    @Test
    void shouldDetectMissingVariable() {
        final Decision decision = addDecisionWithLiteralExpression();

        final List<ValidationResult> validationResults = testee.apply(modelInstance);

        assertEquals(1, validationResults.size());
        final ValidationResult validationResult = validationResults.getFirst();
        assertAll(
                () -> assertEquals(
                        "Decision with a literal expression has no variable name", validationResult.getMessage()),
                () -> assertEquals(decision, validationResult.getElement()),
                () -> assertEquals(Severity.ERROR, validationResult.getSeverity()));
    }

    @Test
    void shouldDetectVariableWithoutName() {
        final Decision decision = addDecisionWithLiteralExpression();
        decision.setVariable(modelInstance.newInstance(Variable.class));

        assertEquals(1, testee.apply(modelInstance).size());
    }

    @Test
    void decisionTablesDoNotNeedVariables() {
        final Decision decision = modelInstance.newInstance(Decision.class);
        decision.addChildElement(modelInstance.newInstance(DecisionTable.class));
        definitions.addChildElement(decision);

        assertEquals(List.of(), testee.apply(modelInstance));
    }

    private Decision addDecisionWithLiteralExpression() {
        final Decision decision = modelInstance.newInstance(Decision.class);
        decision.addChildElement(modelInstance.newInstance(LiteralExpression.class));
        definitions.addChildElement(decision);
        return decision;
    }
}
