package com.inso.MinecraftProject.dto;

import lombok.*;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * DTO Class to transfer mod data between internal services
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DTO {
    private @NonNull List<Mod> mods;
    private @NonNull List<Edge> edges;
    private @Nullable List<Mod> missingDependencies;
    private @Nullable List<String> resolvedDependencies;
}
