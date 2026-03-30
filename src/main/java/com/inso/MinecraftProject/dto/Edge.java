package com.inso.MinecraftProject.dto;
import com.inso.MinecraftProject.entity.Mod;

import lombok.*;

/**
 * DTO Class to represent an edge in the dependency graph
 */
@Data
@Builder
@AllArgsConstructor
public class Edge {
    private @NonNull Mod from;
    private @NonNull Mod to;
    private @NonNull String type;
}
