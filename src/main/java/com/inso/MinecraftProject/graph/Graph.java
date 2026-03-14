package com.inso.MinecraftProject.graph;

import java.util.Map;
import java.util.Set;

import com.inso.MinecraftProject.dto.DTO;
import com.inso.MinecraftProject.entity.Mod;


import java.util.HashMap;
import java.util.HashSet;
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