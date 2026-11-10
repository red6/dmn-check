package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.core.SimpleValidator;
import de.redsix.dmncheck.validators.core.ValidationContext;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.operaton.bpm.model.dmn.instance.Decision;
import org.operaton.bpm.model.dmn.instance.LiteralExpression;
import org.operaton.bpm.model.dmn.instance.Variable;

/** Checks that decisions with a literal expression name their result with a variable. */
public class LiteralExpressionValidator extends SimpleValidator<Decision> {

    @Override
    public Optional<String> documentation() {
        return Optional.of("https://github.com/red6/dmn-check#literal-expressions");
    }

    @Override
    public boolean isApplicable(final Decision decision, final ValidationContext validationContext) {
        return !decision.getChildElementsByType(LiteralExpression.class).isEmpty();
    }

    @Override
    public List<ValidationResult> validate(final Decision decision, final ValidationContext validationContext) {
        final Variable variable = decision.getVariable();
        if (variable == null || variable.getName() == null || variable.getName().isBlank()) {
            return List.of(ValidationResult.init
                    .message("Decision with a literal expression has no variable name")
                    .element(decision)
                    .build());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Class<Decision> getClassUnderValidation() {
        return Decision.class;
    }
}
