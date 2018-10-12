package de.redsix.dmncheck.validators;

import org.camunda.bpm.model.dmn.instance.KnowledgeSource;

public class KnowledgeSourceValidator extends IdAndNameValidator<KnowledgeSource> {

    @Override
    public String getName() {
        return "knowledge source";
    }

    @Override
    public Class<KnowledgeSource> getClassUnderValidation() {
        return KnowledgeSource.class;
    }
}
