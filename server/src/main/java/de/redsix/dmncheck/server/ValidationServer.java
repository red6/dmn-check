package de.redsix.dmncheck.server;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.util.ValidatorLoader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.operaton.bpm.model.dmn.DmnModelException;
import org.operaton.bpm.model.dmn.DmnModelInstance;
import org.operaton.bpm.model.dmn.instance.DrgElement;
import org.operaton.bpm.model.xml.instance.ModelElementInstance;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidationServer {

    @Parameter(names = "-port", description = "Port the service should bind do.")
    public Integer portNumber = 42000;

    @Parameter(names = "-host", description = "Address the service should bind to.")
    public String host = "127.0.0.1";

    private static final int MAX_BODY_BYTES = 10 * 1024 * 1024;

    // Do not validate against the xml schema now. Errors from the xml validation are hard to map back into the editor.
    private static final NonValidatingDmnParser DMN_PARSER = new NonValidatingDmnParser();

    public static void main(String[] args) throws IOException {
        final ValidationServer validationServer = new ValidationServer();

        JCommander.newBuilder().addObject(validationServer).build().parse(args);

        start(validationServer);
    }

    static HttpServer start(final ValidationServer validationServer) throws IOException {
        final Logger logger = LoggerFactory.getLogger(ValidationServer.class);

        final HttpServer server =
                HttpServer.create(new InetSocketAddress(validationServer.host, validationServer.portNumber), 0);
        server.createContext("/validate", exchange -> {
            try (exchange) {
                if (!"POST".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, -1);
                    return;
                }

                final byte[] body;
                try (InputStream requestBody = exchange.getRequestBody()) {
                    body = requestBody.readNBytes(MAX_BODY_BYTES + 1);
                }
                if (body.length > MAX_BODY_BYTES) {
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_ENTITY_TOO_LARGE, -1);
                    return;
                }

                respond(exchange, validationServer.validate(body, logger));
            }
        });
        server.start();
        return server;
    }

    private String validate(final byte[] body, final Logger logger) {
        final ByteArrayInputStream dmnXmlStream = new ByteArrayInputStream(body);

        try {
            final DmnModelInstance modelInstance = DMN_PARSER.parseModelFromStream(dmnXmlStream);
            final List<ValidationResult> validationResults = runValidators(modelInstance);
            return validationResultsToJson(validationResults).toString();
        } catch (DmnModelException e) {
            logger.error(nullsafeError(e.getMessage()));
            return new JSONObject()
                    .put(
                            "items",
                            Collections.singleton(new JSONObject()
                                    .put(
                                            "message",
                                            nullsafeError(ExceptionUtils.getRootCauseMessage(e)))))
                    .toString();
        } catch (Exception e) {
            logger.error(nullsafeError(e.getMessage()));
            return new JSONObject()
                    .put(
                            "items",
                            Collections.singleton(new JSONObject().put("message", nullsafeError(e.getMessage()))))
                    .toString();
        }
    }

    private static void respond(final HttpExchange exchange, final String json) throws IOException {
        final byte[] response = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(response);
        }
    }

    private List<ValidationResult> runValidators(final DmnModelInstance dmnModelInstance) {
        return ValidatorLoader.getValidators().stream()
                .flatMap(validator -> validator.applyWithDocumentation(dmnModelInstance).stream())
                .toList();
    }

    private JSONObject validationResultsToJson(final List<ValidationResult> validationResults) {
        return new JSONObject()
                .put(
                        "items",
                        validationResults.stream()
                                .map(vr -> {
                                    final JSONObject item = new JSONObject()
                                            .put("message", vr.getMessage())
                                            .put("severity", vr.getSeverity().toString());
                                    // Elements without an id are valid input, e.g. for the id validators
                                    Optional.ofNullable(vr.getElement().getAttributeValue("id"))
                                            .ifPresent(id -> item.put("id", id));
                                    getDrgElementParent(vr.getElement())
                                            .map(drgElement -> drgElement.getAttributeValue("id"))
                                            .ifPresent(id -> item.put("drgElementId", id));
                                    vr.getDocumentation().ifPresent(url -> item.put("documentation", url));
                                    return item;
                                })
                                .toList());
    }

    // Results on the definitions element itself, e.g. from DefinitionsIdAndNameValidator, have no DRG element
    private Optional<DrgElement> getDrgElementParent(final @Nullable ModelElementInstance elementInstance) {
        if (elementInstance == null || elementInstance instanceof DrgElement) {
            return Optional.ofNullable((DrgElement) elementInstance);
        } else {
            return this.getDrgElementParent(elementInstance.getParentElement());
        }
    }

    private static String nullsafeError(@Nullable String message) {
        return Optional.ofNullable(message).orElse("Unknown Error");
    }
}
