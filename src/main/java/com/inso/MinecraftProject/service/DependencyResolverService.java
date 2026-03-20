package com.inso.MinecraftProject.service;

import org.springframework.stereotype.Service;
import com.inso.MinecraftProject.entity.Mod;
import com.inso.MinecraftProject.entity.Dependency;
import com.inso.MinecraftProject.dto.ValidationResponse;
import java.util.*;

@Service
public class DependencyResolverService {

    private final ModRepository modRepository;

    public DependencyResolverService(ModRepository modRepository) {
        this.modRepository = modRepository;
    }

    public ValidationResponse validate(Mod mod) {

        List<String> missingDependencies = new ArrayList<>();
        List<String> circularDependencies = new ArrayList<>();

        for (Mod dep : mod.getDepends()) {
            Optional<Mod> dependencyMod = modRepository.findById(dep.getId());

            if (dependencyMod.isEmpty() && dep.isMandatory()) {
                missingDependencies.add(dep.getId());
            }
        }

        detectCircularDependency(mod, new HashSet<>(), circularDependencies);

        return new ValidationResponse(missingDependencies, circularDependencies);
    }

    private void detectCircularDependency(Mod mod, Set<String> visited, List<String> circular) {

        if (visited.contains(mod.getId())) {
            circular.add(mod.getId());
            return;
        }

        visited.add(mod.getId());

        for (Mod dep : mod.getDepends()) {
            modRepository.findById(dep.getId())
                    .ifPresent(m -> detectCircularDependency(m, visited, circular));
        }

        visited.remove(mod.getId());
    }
}