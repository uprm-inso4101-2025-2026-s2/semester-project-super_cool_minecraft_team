package com.inso.MinecraftProject.dto;
import com.inso.MinecraftProject.entity.Mod;

import com.inso.MinecraftProject.entity.Mod;
import lombok.*;
import java.util.List;

/**
 * DTO class for missing dependency results returned to the frontend
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DTO {
    private @NonNull List<Mod> missingDependencies;
    private @NonNull List<String> resolvedDependencies;
}