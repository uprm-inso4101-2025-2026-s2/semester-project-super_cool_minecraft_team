package com.inso.MinecraftProject.dto;

import lombok.*;

import java.util.List;

/**
 * DTO Class to represent a mod and its dependencies
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
}
