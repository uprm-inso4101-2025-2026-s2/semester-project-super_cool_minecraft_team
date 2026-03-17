package com.inso.MinecraftProject.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.inso.MinecraftProject.dto.DTO;
import com.inso.MinecraftProject.entity.Mod;

public class Graph implements GraphI<ModNode> {

    private final Map<String, ModNode> nodes;

    public Graph() {
        this.nodes = new HashMap<>();
    }

    /**
     * Constructs a Graph using data from a DTO object.
     *
     * @param dto the DTO containing mod information
     */
    public Graph(DTO dto) {
        this();

        if (dto == null || dto.getMods() == null) {
            return;
        }

        for (Mod mod : dto.getMods()) {
            Set<String> dependencies = new HashSet<>();
            Set<String> conflicts = new HashSet<>();

            if (mod.getDepends() != null) {
                for (Mod dependency : mod.getDepends()) {
                    dependencies.add(dependency.getId() + "@" + dependency.getVersion());
                }
            }

            if (mod.getConflicts() != null) {
                for (Mod conflict : mod.getConflicts()) {
                    conflicts.add(conflict.getId() + "@" + conflict.getVersion());
                }
            }

            ModNode node = new ModNode(
                    mod.getId(),
                    mod.getVersion(),
                    dependencies,
                    conflicts
            );

            String key = mod.getId() + "@" + mod.getVersion();
            nodes.put(key, node);
        }
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
    public ModNode findNode(String key) {
        return nodes.get(key);
    }

    @Override
    public Iterator<ModNode> iterator() {
        return nodes.values().iterator();
    }
}