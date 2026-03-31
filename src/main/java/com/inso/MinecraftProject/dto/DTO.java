package com.inso.MinecraftProject.dto;

import com.inso.MinecraftProject.entity.Mod;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO class to transfer dependency graph data to the frontend
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DTO {

    @Builder.Default
    private List<Mod> mods = new ArrayList<>();

    @Builder.Default
    private List<Edge> edges = new ArrayList<>();

    @Builder.Default
    private List<Mod> missingDependencies = new ArrayList<>();

    @Builder.Default
    private List<String> resolvedDependencies = new ArrayList<>();
}