package com.inso.MinecraftProject.graph;

import java.util.Iterator;

public interface GraphI <T extends ModNodeI> extends Iterable<T> {

    /**
     * Generates a unique key for a mod
     *
     * @param modNode the mod node for which to generate the key
     * @return a unique key in the format modid@version
     * @throws IllegalArgumentException if the modNode is null or has invalid modId/version
     */
    String generateKey(T modNode) throws IllegalArgumentException;

    /**
     * Adds a new mod node to the graph.
     *
     * @param modNode the mod node to add
     * @return true if the node was successfully added, false if a node with the same key already exists
     */
    boolean addNode(T modNode);

    /**
     * Removes a node from the graph.
     *
     * @param key the node key (modid@version)
     * @return true if the node was successfully removed, false if the node was not found
     */
    boolean removeNode(String key);

    /**
     * Finds a node in the graph using its key.
     *
     * @param key the node key (modid@version)
     * @return the ModNode if found, otherwise null
     */
    T findNode(String key);

    /**
     * Adds a new mod node to the graph.
     *
     * @param key the node key (modid@version)
     * @return true if the node was found, false if the node was not found in the dependency graph
     */
    boolean hasModNode(String key);

    /**
     * Adds a new mod node to the graph.
     *
     * @param dependency the dependency to find 
     * @param modNode the mod node from which the dependencies are extracted from
     * @return true if the dependency relationship was found, false if the dependency was not found in the dependency graph
     */
    boolean hasDependency(String dependency, ModNode modNode);

    /**
     * Returns an iterator over all ModNode objects in the graph.
     *
     * @return iterator of ModNode
     */
    @Override
    Iterator<T> iterator();

}
