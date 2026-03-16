package com.inso.MinecraftProject.graph;

import java.util.*;

/**
 * ModNode is a class to represent mods in the graph data structure, each mod will be represented by a ModNode object
 * containing the mod ID, version, and a collection of its dependencies and conflicts.
 */

public class ModNode {
     // ModNode class attributes
     private String modID;
     private String modVersion;
     private Set<String> dependencies;
     private Set<String> conflicts;
 
     // Constructor for the ModNode class
 
     public ModNode(String modID, String modVersion, Set<String> dependencies, Set<String> conflicts) {
         this.modID = modID; 
         this.modVersion = modVersion;
         this.dependencies = dependencies;
         this.conflicts = conflicts;
     }
 
     /*
     * @returns the unique identifier of the mod 
     */
     public String getModId() {
         return modID;
     }
 
     /*
     * @returns the version of the mod 
     */
     public String getVersion() {
         return modVersion;
     }
 
     /*
     * @return A set of mod IDs that this mod depends on. Each dependency is represented as a string in the format "modId:version".
     */
     public Set<String> getDependencies() {
         return dependencies;
     }
 
     /*
     * @return A set of mod IDs that this mod conflicts with. Each conflict is represented as a string in the format "modId:version".
     */
 
     public Set<String> getConflicts() {
         return conflicts;
     }

}
