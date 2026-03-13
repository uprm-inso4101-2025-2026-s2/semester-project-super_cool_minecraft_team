package com.inso.MinecraftProject.graph;

import java.util.Map;
import java.util.HashMap;

/**
 * Graph class responsible for managing all ModNode objects
 * in the mod dependency graph.
 */
public class Graph {

    /**
     * Internal map used to store all mod nodes in the graph.
     */
    private final Map<String, ModNode> nodes;

    /**
     * Constructs an empty Graph and initializes its internal storage.
     */
    public Graph() {
        this.nodes = new HashMap<>();
    }
}