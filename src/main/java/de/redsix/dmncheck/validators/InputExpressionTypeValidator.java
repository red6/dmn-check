package de.redsix.dmncheck.validators;

import org.camunda.bpm.model.dmn.instance.InputExpression;

public enum InputExpressionTypeValidator implements ExpressionTypeValidator<InputExpression> {
    instance;

    @Override
    public Class<InputExpression> getClassUnderValidation() {
        return InputExpression.class;
    }
}
