package com.inso.MinecraftProject.service;

import java.util.HashMap;

@ Repository

public class Repository {
    
    private final Map<String,Mod> mods = new HashMap<>();

    public void Save(Mod mod) {
        mod.put(mod.getModId(),mod);
    }

    public Optional<Mod> findbyID(String id) {

        return(Optional.ofNullable(mods.get(id)));
    }

    public Collection<Mod> findAll() {

        return mods.values();
    }
}
