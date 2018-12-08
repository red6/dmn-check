package de.redsix.dmncheck.validators;

import org.camunda.bpm.model.dmn.instance.Output;


public class OutputTypeDeclarationValidator extends ElementTypeDeclarationValidator<Output> {

    @Override
    public String getTypeRef(Output output) {
        return output.getTypeRef();
    }

    @Override
    public Class<Output> getClassUnderValidation() {
        return Output.class;
    }
}
