package de.redsix.dmncheck.validators;

import org.camunda.bpm.model.dmn.instance.InputExpression;

public enum InputExpressionTypeDeclarationValidator implements ExpressionTypeDeclarationValidator<InputExpression> {
    instance;

    @Override
    public Class<InputExpression> getClassUnderValidation() {
        return InputExpression.class;
    }
}
