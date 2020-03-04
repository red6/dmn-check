package de.redsix.dmncheck.server;

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

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static spark.Spark.post;

public class ValidationServer {

    public static void main(String[] args) {
        ValidationServer validationServer = new ValidationServer();

        post("/validate", (request, response) -> {

            final ByteArrayInputStream dmnXmlStream = new ByteArrayInputStream(request.body().getBytes(StandardCharsets.UTF_8));

            try {
                // Do not validate against the xml schema now. Errors from the xml validation are hard to map back into the editor.
                FieldUtils.writeField(Dmn.INSTANCE, "dmnParser", new NonValidatingDmnParser(), true);

                final DmnModelInstance modelInstance = Dmn.readModelFromStream(dmnXmlStream);
                final List<ValidationResult> validationResults = validationServer.runValidators(modelInstance);
                return validationServer.validationResultsToJson(validationResults).toString();
            } catch (DmnModelException e) {
                e.printStackTrace();
                return new JSONObject().put("items", Collections
                        .singleton(new JSONObject().put("message", ExceptionUtils.getRootCause(e).getMessage())));
            } catch (Exception e) {
                e.printStackTrace();
                return new JSONObject().put("items", Collections.singleton(new JSONObject().put("message", e.getMessage())));
            }
        });
    }

    private List<ValidationResult> runValidators(final DmnModelInstance dmnModelInstance) {
        return ValidatorLoader.getValidators().stream()
                                .flatMap(validator -> validator.apply(dmnModelInstance).stream())
                                .collect(Collectors.toList());
    }

    private JSONObject validationResultsToJson(final List<ValidationResult> validationResults) {
        return new JSONObject().put("items", validationResults.stream().map(vr -> new JSONObject().put("id", vr.getElement()
                                                                                                               .getAttributeValue("id"))
                                                                                                  .put("drgElementId", getDrgElementParent(vr.getElement()).getAttributeValue("id"))
                                                                                                  .put("message", vr.getMessage())
                                                                                                  .put("severity",
                                                                                                       vr.getSeverity().toString()))
                                                              .collect(Collectors.toList()));
    }

    private DrgElement getDrgElementParent(final ModelElementInstance elementInstance) {

        if (elementInstance instanceof DrgElement) {
            return (DrgElement) elementInstance;
        } else {
            return this.getDrgElementParent(elementInstance.getParentElement());
        }
    }
}
