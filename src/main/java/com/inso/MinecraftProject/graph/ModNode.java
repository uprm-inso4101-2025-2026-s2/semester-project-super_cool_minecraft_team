package com.inso.MinecraftProject.graph;

import java.util.Set;

public class ModNode implements ModNodeI {
    private String modID;
    private String modVersion;
    private Set<String> dependencies;
    private Set<String> conflicts;

    public ModNode(String modID, String modVersion, Set<String> dependencies, Set<String> conflicts) {
        this.modID = modID;
        this.modVersion = modVersion;
        this.dependencies = dependencies;
        this.conflicts = conflicts;
    }

    public String getModId() {
        return modID;
    }

    public String getVersion() {
        return modVersion;
    }

    public Set<String> getDependencies() {
        return dependencies;
    }

    public Set<String> getConflicts() {
        return conflicts;
    }
}