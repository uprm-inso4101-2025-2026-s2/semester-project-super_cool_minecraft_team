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
        return false; //dummy return
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