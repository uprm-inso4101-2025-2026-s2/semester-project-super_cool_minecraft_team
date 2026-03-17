package com.inso.MinecraftProject.dto;

public record MissingDependencyDto(
        String id,
        String name,
        String requiredVersion,
        String loader,
        String mcVersion
) {
}
