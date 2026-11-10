package de.redsix.dmncheck.drg;

import static org.junit.jupiter.api.Assertions.*;

import org.operaton.bpm.model.dmn.Dmn;
import org.operaton.bpm.model.dmn.DmnModelInstance;
import org.junit.jupiter.api.Test;

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
