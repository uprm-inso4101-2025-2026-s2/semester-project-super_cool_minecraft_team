package com.inso.MinecraftProject.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class Dependency {

    private String modId;      // ID of the mod this dependency is neccesary
    private boolean mandatory; // true if this dependency is required

}
