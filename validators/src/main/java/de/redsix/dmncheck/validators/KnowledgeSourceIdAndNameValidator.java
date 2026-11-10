package de.redsix.dmncheck.validators;

import org.operaton.bpm.model.dmn.instance.KnowledgeSource;

public class KnowledgeSourceIdAndNameValidator extends IdAndNameValidator<KnowledgeSource> {

    @Override
    public String getName() {
        return "knowledge source";
    }

    @Override
    public Class<KnowledgeSource> getClassUnderValidation() {
        return KnowledgeSource.class;
    }
}
