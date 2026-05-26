package com.inso.MinecraftProject.graph;

import java.util.Set;

/**
 * ModNode interface representing a ModNode in the graph data structure, each mod will be represented by a ModNode
 * containing the mod ID, version, and a collection of its dependencies and conflicts.
 */
public interface ModNodeI {

    /**
     * @return The unique identifier of the mod
     */
    String getModId();

    /**
     * @return The version of the mod
     */
    String getVersion();

    /**
     * @return A set of mod IDs that this mod depends on. Each dependency is represented as a string in the format "modId:version".
     */
    Set<String> getDependencies();

    /**
     * @return A set of mod IDs that this mod conflicts with. Each conflict is represented as a string in the format "modId:version".
     */
    Set<String> getConflicts();

}
