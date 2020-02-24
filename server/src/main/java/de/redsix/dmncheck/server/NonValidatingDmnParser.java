package de.redsix.dmncheck.server;

import org.camunda.bpm.model.dmn.impl.DmnParser;
import org.camunda.bpm.model.xml.instance.DomDocument;

public class NonValidatingDmnParser extends DmnParser {

    @Override
    public void validateModel(DomDocument document) {

    }

}
