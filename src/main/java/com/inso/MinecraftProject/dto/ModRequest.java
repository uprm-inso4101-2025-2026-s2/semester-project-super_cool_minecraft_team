package com.inso.MinecraftProject.dto;

import java.util.List;

import com.inso.MinecraftProject.entity.Dependency;

import lombok.Data;
import lombok.NonNull;

@Data
public class ModRequest {

    @NonNull
    private String modId;

    @NonNull
    private String version;

    private String minecraftVersion;

    private List<Dependency> dependencies;

}
