package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.drg.RequirementGraph;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.core.RequirementGraphValidator;
import org.camunda.bpm.model.dmn.instance.DrgElement;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.DepthFirstIterator;

import java.util.Collections;
import java.util.List;

public class RequirementGraphLeafValidator extends RequirementGraphValidator {

    @Override
    public List<ValidationResult> validate(RequirementGraph drg) {
        final DepthFirstIterator<DrgElement, DefaultEdge> iterator = new DepthFirstIterator<>(drg);

        int numberLeafNodes = 0;
        while (iterator.hasNext()) {
            // Self-loops are not allowed in requirement graphs therefore it is
            // sufficient to check if the out degree of the node is zero.
            if (drg.outDegreeOf(iterator.next()) == 0) {
                numberLeafNodes++;
            }
        }

        if (numberLeafNodes == 1) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(ValidationResult.init
                    .message("Requirement graphs may only contain one leaf node")
                    .element(drg.iterator().next())
                    .build());
        }
    }
}
