package com.inso.MinecraftProject.graph;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

public class Graph implements GraphI<ModNode> {

    private final Map<String, ModNode> nodes;

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
    public boolean addNode(ModNode modNode) {
        String key = generateKey(modNode);

        if (nodes.containsKey(key)) {
            return false;
        }

        nodes.put(key, modNode);
        return true;
    }

    @Override
    public boolean removeNode(String key) {
        return nodes.remove(key) != null;
    }

    @Override
    public ModNode findNode(String key) {
        return nodes.get(key);
    }

    @Override
    public Iterator<ModNode> iterator() {
        return nodes.values().iterator();
    }
}