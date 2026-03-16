package com.inso.MinecraftProject.graph;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Graph class responsible for managing all ModNode objects
 * in the mod dependency graph.
 */
public class Graph implements GraphI<ModNode> {

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
    @Override
    public String generateKey(ModNode modNode) throws IllegalArgumentException {
        if (modNode == null || modNode.getModId() == null || modNode.getVersion() == null) {
            throw new IllegalArgumentException("ModNode and its modId/version cannot be null");
        }
        return modNode.getModId() + "@" + modNode.getVersion();
    }

    @Override
    public boolean addNode(ModNode modNode){
        return false; //dummy return
    }

    @Override
    public boolean removeNode(String key){
        if (key == null || !nodes.containsKey(key)) {
            return false;
        }
        // Remove the node from all dependencies and conflicts of other nodes
        for (ModNode node : nodes.values()) {        
            node.getDependencies().remove(key);
            node.getConflicts().remove(key);
        }
        // Remove the node from the graph
        nodes.remove(key);
        return true;
    }

    @Override
    public ModNode findNode(String key){
        return null; //dummy return
    }

    @Override
    public Iterator<ModNode> iterator(){
        return null; //dummy return
    }

}