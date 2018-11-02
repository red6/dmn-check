package de.redsix.dmncheck.drg;

import org.camunda.bpm.model.dmn.instance.DrgElement;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;

public class RequirementGraph extends DirectedAcyclicGraph<DrgElement, DefaultEdge> {

    public RequirementGraph(Class<? extends DefaultEdge> edgeClass) {
        super(edgeClass);
    }
}
