package com.inso.MinecraftProject.view;

public record MissingDependencyItem(
        String name,
        String version,
        String loader,
        String downloadUrl,
        boolean resolved
) {
}