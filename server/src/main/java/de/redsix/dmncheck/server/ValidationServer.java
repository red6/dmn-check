package de.redsix.dmncheck.server;

import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.core.Validator;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelInstance;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static spark.Spark.post;

public class ValidationServer {

    private static final String VALIDATOR_PACKAGE = "de.redsix.dmncheck.validators";
    private static final String VALIDATOR_CORE_PACKAGE = "de.redsix.dmncheck.validators.core";

    private List<Validator> validators;

    public static void main(String[] args) {
        ValidationServer validationServer = new ValidationServer();

        post("/validate", (request, response) -> {

            final ByteArrayInputStream dmnXmlStream = new ByteArrayInputStream(request.body().getBytes(StandardCharsets.UTF_8));
            final DmnModelInstance modelInstance = Dmn.readModelFromStream(dmnXmlStream);

            return validationServer.runValidators(modelInstance).toString();
        });
    }

    private List<ValidationResult> runValidators(final DmnModelInstance dmnModelInstance) {
        return getValidators().stream()
                              .flatMap(validator -> validator.apply(dmnModelInstance).stream())
                              .collect(Collectors.toList());
    }

    private List<Validator> getValidators() {
        if (validators != null) {
            return validators;
        }

        final ScanResult scanResult = new ClassGraph()
                .whitelistClasses(Validator.class.getName())
                .whitelistPackages(VALIDATOR_CORE_PACKAGE)
                .whitelistPackagesNonRecursive(VALIDATOR_PACKAGE, VALIDATOR_PACKAGE + ".core")
                .scan();

        final ClassInfoList allValidatorClasses = scanResult.getClassesImplementing(Validator.class.getName());

        validators = allValidatorClasses.loadClasses(Validator.class).stream()
                                        .filter(validatorClass -> !Modifier.isAbstract(validatorClass.getModifiers()))
                                        .filter(validatorClass -> !Modifier.isInterface(validatorClass.getModifiers()))
                                        .map(this::instantiateValidator)
                                        .collect(Collectors.toList());

        return validators;
    }

    private Validator instantiateValidator(final Class<? extends Validator> validator) {
        try {
            return validator.newInstance();
        }
        catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Failed to load validator " + validator, e);
        }
    }
}
