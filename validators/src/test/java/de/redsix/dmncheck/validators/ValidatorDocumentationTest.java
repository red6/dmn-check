package de.redsix.dmncheck.validators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.util.ValidatorLoader;
import de.redsix.dmncheck.validators.core.Validator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.instance.Definitions;
import org.junit.jupiter.api.Test;

class ValidatorDocumentationTest {

    private static final String README_URL = "https://github.com/red6/dmn-check#";

    @Test
    void everyValidatorLinksToAnExistingReadmeSection() throws IOException {
        // GitHub derives anchors from headings: lower case, punctuation removed, spaces replaced by hyphens
        final Set<String> anchors = Files.readAllLines(Path.of("../README.md")).stream()
                .filter(line -> line.startsWith("#"))
                .map(line -> line.replaceFirst("^#+", "")
                        .trim()
                        .toLowerCase()
                        .replaceAll("[^a-z0-9 -]", "")
                        .replace(' ', '-'))
                .collect(Collectors.toSet());

        for (final Validator validator : ValidatorLoader.getValidators()) {
            final String documentation = validator
                    .documentation()
                    .orElseThrow(() -> new AssertionError(validator.getClass() + " has no documentation"));

            assertTrue(documentation.startsWith(README_URL), documentation);
            assertTrue(
                    anchors.contains(documentation.substring(README_URL.length())),
                    validator.getClass() + " links to missing section " + documentation);
        }
    }

    @Test
    void attachesDocumentationToValidationResults() {
        final DmnModelInstance modelInstance = Dmn.createEmptyModel();
        modelInstance.setDefinitions(modelInstance.newInstance(Definitions.class));

        final NoDecisionPresentValidator testee = new NoDecisionPresentValidator();
        final List<ValidationResult> validationResults = testee.applyWithDocumentation(modelInstance);

        assertEquals(1, validationResults.size());
        assertEquals(testee.documentation(), validationResults.getFirst().getDocumentation());
        assertEquals(Optional.empty(), testee.apply(modelInstance).getFirst().getDocumentation());
    }
}
