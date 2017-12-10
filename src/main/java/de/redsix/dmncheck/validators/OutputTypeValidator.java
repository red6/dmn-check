package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.model.ExpressionTypeEnum;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.core.Validator;
import org.camunda.bpm.model.dmn.instance.Output;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public enum OutputTypeValidator implements ElementTypeDeclarationValidator<Output> {
    instance;

    @Override
    public String getTypeRef(Output output) {
        return output.getTypeRef();
    }

    @Override
    public Class<Output> getClassUnderValidation() {
        return Output.class;
    }
}
