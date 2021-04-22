package de.redsix.dmncheck.server;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.util.ValidatorLoader;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelException;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.instance.DrgElement;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static spark.Spark.port;
import static spark.Spark.post;

public class ValidationServer {

    @Parameter(names = "-port", description = "Port the service should bind do.")
    public Integer portNumber = 42000;

    public static void main(String[] args) {
        final ValidationServer validationServer = new ValidationServer();

        final Logger logger = LoggerFactory.getLogger(ValidationServer.class);

        JCommander
            .newBuilder()
            .addObject(validationServer)
            .build()
            .parse(args);

        port(validationServer.portNumber);
        post("/validate", (request, response) -> {

            final ByteArrayInputStream dmnXmlStream = new ByteArrayInputStream(request.body().getBytes(StandardCharsets.UTF_8));

            try {
                // Do not validate against the xml schema now. Errors from the xml validation are hard to map back into the editor.
                FieldUtils.writeField(Dmn.INSTANCE, "dmnParser", new NonValidatingDmnParser(), true);

                final DmnModelInstance modelInstance = Dmn.readModelFromStream(dmnXmlStream);
                final List<ValidationResult> validationResults = validationServer.runValidators(modelInstance);
                return validationServer.validationResultsToJson(validationResults).toString();
            } catch (DmnModelException e) {
                logger.error(nullsafeError(e.getMessage()));
                return new JSONObject()
                    .put("items", Collections.singleton(new JSONObject().put("message", nullsafeError(ExceptionUtils.getRootCause(e).getMessage()))));
            } catch (Exception e) {
                logger.error(nullsafeError(e.getMessage()));
                return new JSONObject()
                    .put("items", Collections.singleton(new JSONObject().put("message", nullsafeError(e.getMessage()))));
            }
        });
    }

    private List<ValidationResult> runValidators(final DmnModelInstance dmnModelInstance) {
        return ValidatorLoader.getValidators().stream()
                                .flatMap(validator -> validator.apply(dmnModelInstance).stream())
                                .collect(Collectors.toList());
    }

    private JSONObject validationResultsToJson(final List<ValidationResult> validationResults) {
        return new JSONObject().put("items", validationResults
            .stream()
            .map(vr -> new JSONObject()
                .put("id", vr.getElement().getAttributeValue("id"))
                .put("drgElementId", getDrgElementParent(vr.getElement()).getAttributeValue("id"))
                .put("message", vr.getMessage())
                .put("severity", vr.getSeverity().toString()))
            .collect(Collectors.toList()));
    }

    private DrgElement getDrgElementParent(final ModelElementInstance elementInstance) {

        if (elementInstance instanceof DrgElement) {
            return (DrgElement) elementInstance;
        } else {
            return this.getDrgElementParent(elementInstance.getParentElement());
        }
    }

    private static String nullsafeError(@Nullable String message) {
        return Optional.ofNullable(message).orElse("Unknown Error");
    }
}
