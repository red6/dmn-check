package de.redsix.dmncheck.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ValidationServerTest {

    // Uses the camunda namespace like files created with the Camunda Modeler
    private static final String DMN_WITH_EMPTY_DECISION_TABLE =
            """
            <?xml version="1.0" encoding="UTF-8"?>
            <definitions xmlns="https://www.omg.org/spec/DMN/20191111/MODEL/"
                         xmlns:camunda="http://camunda.org/schema/1.0/dmn"
                         id="definitions" name="Definitions" namespace="http://camunda.org/schema/1.0/dmn">
              <decision id="decision" name="Decision">
                <decisionTable id="decisionTable">
                  <input id="input" label="Season" camunda:inputVariable="season">
                    <inputExpression id="inputExpression" typeRef="string">
                      <text>season</text>
                    </inputExpression>
                  </input>
                  <output id="output" label="Dish" name="dish" typeRef="string" />
                </decisionTable>
              </decision>
            </definitions>
            """;

    private static HttpServer server;
    private static URI validateUri;
    private static final HttpClient client = HttpClient.newHttpClient();

    @BeforeAll
    static void startServer() throws IOException {
        final ValidationServer validationServer = new ValidationServer();
        validationServer.portNumber = 0;
        server = ValidationServer.start(validationServer);
        validateUri = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/validate");
    }

    @AfterAll
    static void stopServer() {
        server.stop(0);
    }

    @Test
    void reportsValidationResultsForDrgElements() throws Exception {
        final HttpResponse<String> response = post(DMN_WITH_EMPTY_DECISION_TABLE);

        assertEquals(200, response.statusCode());
        final JSONArray items = new JSONObject(response.body()).getJSONArray("items");
        assertEquals(1, items.length(), response.body());

        final JSONObject item = items.getJSONObject(0);
        assertEquals("Decision table has no rules", item.getString("message"));
        assertEquals("WARNING", item.getString("severity"));
        assertEquals("decisionTable", item.getString("id"));
        assertEquals("decision", item.getString("drgElementId"));
        assertTrue(item.getString("documentation").startsWith("https://github.com/red6/dmn-check#"));
    }

    @Test
    void reportsUnparsableInputAsMessage() throws Exception {
        final HttpResponse<String> response = post("not xml");

        assertEquals(200, response.statusCode());
        final JSONObject item = new JSONObject(response.body()).getJSONArray("items").getJSONObject(0);
        assertTrue(item.getString("message").contains("Content is not allowed in prolog"), item.toString());
        assertFalse(item.has("drgElementId"));
    }

    @Test
    void rejectsOtherMethodsThanPost() throws Exception {
        final HttpResponse<String> response = client.send(
                HttpRequest.newBuilder(validateUri).GET().build(), HttpResponse.BodyHandlers.ofString());

        assertEquals(405, response.statusCode());
    }

    private static HttpResponse<String> post(final String body) throws Exception {
        return client.send(
                HttpRequest.newBuilder(validateUri)
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
    }
}
