package com.inso.MinecraftProject.entity;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Entity Class to represent a mod and its dependencies
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Mod {
    private @NonNull String id;
    private @NonNull String version;

    private @NonNull List<Mod> depends;
    private @NonNull List<Mod> breaks;
    private @NonNull List<Mod> suggests;
    private @NonNull List<Mod> recommends;
    private @NonNull List<Mod> conflicts;

    public boolean isMandatory() {
        return depends != null && !depends.isEmpty();
    }

    public boolean isMandatory1() {
        return isMandatory();
    }

    public List<Mod> getConflicts() {
        return conflicts;
    }

    public String getId() {
        return id;
    }

    public List<Mod> getDepends() {
        return depends;
    }

    public List<Mod> getBreaks() {
        return breaks;
    }

    public List<Mod> getSuggests() {
        return suggests;
    }

    public String getVersion() {
        return version;
    }

    public List<Mod> getRecommends() {
        return recommends;
    }
}
