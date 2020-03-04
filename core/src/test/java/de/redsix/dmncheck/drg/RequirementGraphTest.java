package de.redsix.dmncheck.drg;

import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequirementGraphTest {

    @Test
    void emptyGraphFromEmptyModel() {
        // Arrange
        final DmnModelInstance emptyModel = Dmn.createEmptyModel();

        // Act
        final RequirementGraph requirementGraph = RequirementGraph.from(emptyModel);

        // Assert
        assertTrue(requirementGraph.vertexSet().isEmpty());
        assertTrue(requirementGraph.edgeSet().isEmpty());
    }

}
