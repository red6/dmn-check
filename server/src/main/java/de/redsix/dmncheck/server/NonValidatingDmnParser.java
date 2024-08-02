package de.redsix.dmncheck.server;

import org.camunda.bpm.model.dmn.impl.DmnParser;
import org.camunda.bpm.model.xml.instance.DomDocument;

public class NonValidatingDmnParser extends DmnParser {

    @Override
    public void validateModel(DomDocument document) {
        // Do not validate against the xml schema for now. Errors from the xml validation are hard to map back
        // into the editor.
    }
}
