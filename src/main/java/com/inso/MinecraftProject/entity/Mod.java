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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isMandatory'");
    }

    public boolean isMandatory1() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<Mod> getConflicts() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<Mod> getDepends() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<Mod> getBreaks() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<Mod> getSuggests() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object getVersion() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<Mod> getRecommends() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
