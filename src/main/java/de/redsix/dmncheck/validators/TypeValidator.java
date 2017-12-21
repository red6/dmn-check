package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.feel.FeelParser;
import de.redsix.dmncheck.feel.FeelTypecheck;
import de.redsix.dmncheck.model.ExpressionType;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.util.Either;
import de.redsix.dmncheck.util.Eithers;
import de.redsix.dmncheck.util.Util;
import de.redsix.dmncheck.validators.core.Validator;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.DmnElement;
import org.camunda.bpm.model.dmn.instance.Rule;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public interface TypeValidator extends Validator<DecisionTable> {

    String errorMessage();

    boolean isEmptyAllowed();

    default Stream<ValidationResult> typecheck(final Rule rule, final Stream<? extends DmnElement> expressions,
            final Stream<ExpressionType> types) {
        return Util.zip(expressions, types, (inputEntry, expectedType) -> {

            final Either<ExpressionType, ValidationResult.Builder> typedcheckResult = FeelTypecheck
                    .typecheck(FeelParser.PARSER.parse(inputEntry.getTextContent()));

            return Eithers.caseOf(typedcheckResult)
                    .left(type -> {
                        if (type.equals(expectedType) || isEmptyAllowed() && type.equals(ExpressionType.TOP)) {
                            return Collections.<ValidationResult.Builder>emptyList();
                        } else {
                            return Collections.singletonList(ValidationResult.Builder.with($ -> {
                                $.message = errorMessage();
                                $.element = rule;
                            }));
                        }})
                    .right(validationResultBuilder -> Collections.singletonList(validationResultBuilder.extend($ -> $.element = rule)));
        }).flatMap(List::stream).map(ValidationResult.Builder::build);
    }

    @Override
    default Class<DecisionTable> getClassUnderValidation() {
        return DecisionTable.class;
    }
}
