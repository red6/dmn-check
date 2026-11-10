package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.drg.RequirementGraph;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.core.RequirementGraphValidator;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.operaton.bpm.model.dmn.DmnModelInstance;
import org.operaton.bpm.model.dmn.instance.DrgElement;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.DepthFirstIterator;

public class RequirementGraphLeafValidator extends RequirementGraphValidator {

    @Override
    public Optional<String> documentation() {
        return Optional.of("https://github.com/red6/dmn-check#correctly-connected-requirement-graphs");
    }

    @Override
    public List<ValidationResult> apply(final DmnModelInstance dmnModelInstance) {
        try {
            return validate(RequirementGraph.from(dmnModelInstance));
        } catch (IllegalArgumentException exception) {
            // Already reported by ConnectedRequirementGraphValidator, e.g. if the graph contains a cycle
            return Collections.emptyList();
        }
    }

    @Override
    public List<ValidationResult> validate(final RequirementGraph drg) {
        final DepthFirstIterator<DrgElement, DefaultEdge> iterator = new DepthFirstIterator<>(drg);

        int numberLeafNodes = 0;
        while (iterator.hasNext()) {
            // Self-loops are not allowed in requirement graphs therefore it is
            // sufficient to check if the out degree of the node is zero.
            if (drg.outDegreeOf(iterator.next()) == 0) {
                numberLeafNodes++;
            }
        }

        if (numberLeafNodes < 2) {
            return Collections.emptyList();
        } else {
            return List.of(ValidationResult.init
                    .message("Requirement graphs may only contain one leaf node")
                    .element(drg.getDefinitions())
                    .build());
        }
    }
}
