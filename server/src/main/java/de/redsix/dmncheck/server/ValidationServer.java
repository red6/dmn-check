package de.redsix.dmncheck.server;

import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.util.ValidatorLoader;
import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelInstance;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static spark.Spark.post;

public class ValidationServer {

    public static void main(String[] args) {
        ValidationServer validationServer = new ValidationServer();

        post("/validate", (request, response) -> {

            final ByteArrayInputStream dmnXmlStream = new ByteArrayInputStream(request.body().getBytes(StandardCharsets.UTF_8));
            final DmnModelInstance modelInstance = Dmn.readModelFromStream(dmnXmlStream);

            return validationServer.runValidators(modelInstance).toString();
        });
    }

    private List<ValidationResult> runValidators(final DmnModelInstance dmnModelInstance) {
        return ValidatorLoader.getValidators().stream()
                                .flatMap(validator -> validator.apply(dmnModelInstance).stream())
                                .collect(Collectors.toList());
    }
}
