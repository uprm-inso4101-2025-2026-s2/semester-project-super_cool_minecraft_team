package com.inso.MinecraftProject.dto;

import java.util.List;

public record ResolvedDependencyDto(
        String id,
        String name,
        List<String> links,
        String preferred
) {
}
