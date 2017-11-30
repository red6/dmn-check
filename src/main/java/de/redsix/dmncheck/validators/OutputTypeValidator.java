package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.model.ExpressionTypeEnum;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.core.Validator;
import org.camunda.bpm.model.dmn.instance.Output;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public enum OutputTypeValidator implements Validator<Output> {
    instance;

    @Override
    public Class<Output> getClassUnderValidation() {
        return Output.class;
    }

    public boolean isApplicable(Output output) {
        return true;
    }

    public List<ValidationResult> validate(Output output) {
        final String outputType = output.getTypeRef();
        if (Objects.isNull(outputType)) {
            return Collections.singletonList(ValidationResult.Builder.with($ -> {
                $.message = "Output has no type";
                $.element = output;
            }).build());
        } else if (ExpressionTypeEnum.isValid(outputType)) {
            return Collections.singletonList(ValidationResult.Builder.with($ -> {
                $.message = "Output uses an unsupported type";
                $.element = output;
            }).build());
        } else {
            return Collections.emptyList();
        }
    }
}
