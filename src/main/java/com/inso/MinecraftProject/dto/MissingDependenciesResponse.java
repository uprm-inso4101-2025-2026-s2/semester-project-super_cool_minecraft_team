package com.inso.MinecraftProject.dto;

import java.util.List;

public record MissingDependenciesResponse(
        List<MissingDependencyDto> missingDependencies,
        List<ResolvedDependencyDto> resolvedDependencies,
        boolean analysisHasPartialResults
) {
}
