package com.inso.MinecraftProject.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ValidationResponse {

    private List<String> missingDependencies;

    private List<String> circularDependencies;

}
