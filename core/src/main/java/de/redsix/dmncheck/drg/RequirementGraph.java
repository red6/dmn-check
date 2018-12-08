package de.redsix.dmncheck.drg;

import org.camunda.bpm.model.dmn.instance.Definitions;
import org.camunda.bpm.model.dmn.instance.DrgElement;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;

public class RequirementGraph extends DirectedAcyclicGraph<DrgElement, DefaultEdge> {

    private Definitions definitions;

    public RequirementGraph(Class<? extends DefaultEdge> edgeClass, Definitions definitions) {
        super(edgeClass);
        this.definitions = definitions;
    }

    public Definitions getDefinitions() {
        return definitions;
    }
}
