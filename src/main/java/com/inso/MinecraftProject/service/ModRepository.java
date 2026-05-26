package com.inso.MinecraftProject.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.inso.MinecraftProject.entity.Mod;

@Repository
public class ModRepository{

    private final Map<String, Mod> mods = new HashMap<>();

    public void save(Mod mod) {
        mods.put(mod.getId(), mod);
    }

    public Optional<Mod> findById(String id) {
        return Optional.ofNullable(mods.get(id));
    }

    public Collection<Mod> findAll() {
        return mods.values();
    }
}