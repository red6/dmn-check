package de.redsix.dmncheck.validators;

import org.camunda.bpm.model.dmn.instance.InputExpression;

public enum InputExpressionTypeDeclarationValidator implements ElementTypeDeclarationValidator<InputExpression> {
    instance;

    @Override
    public String getTypeRef(InputExpression inputExpression) {
        return inputExpression.getTypeRef();
    }

    @Override
    public Class<InputExpression> getClassUnderValidation() {
        return InputExpression.class;
    }
}
