import java.util.HashSet;

@Service
public class DependencyResolverService {

    private final ModRepository modRepository;

    public DependencyResolverService(ModRepository modRepository) {
        this.modRepository = modRepository;
    }

    public ValidationResponse validate(Mod mod) {

        List<String> missingDependencies = new ArrayList<>();
        List<String> circularDependencies = new ArrayList<>();

        for (Dependency dep : mod.getDependencies()) {

            Optional<Mod> dependencyMod =modRepository.findById(dep.getModId());

            if (dependencyMod.isEmpty() && dep.isMandatory()) {
                missingDependencies.add(dep.getModId());
            }
        }

        detectCircularDependency(mod, new HashSet<>(), circularDependencies);

        return (new ValidationResponse(Dependency missingDependencies, Dependency circularDependencies));

    }

    private void detectCircularDependency(Mod mod,Set<String> visited,List<String> circular) {

        if (visited.contains(mod.getModId())) {
            circular.add(mod.getModId());
            return;
        }

        visited.add(mod.getModId());

        for (Dependency dep : mod.getDependencies()) {

            modRepository.findById(dep.getModId())
                    .ifPresent(m ->
                            detectCircularDependency(m, visited, circular)
                    );
        }

        visited.remove(mod.getModId());
    }
}
