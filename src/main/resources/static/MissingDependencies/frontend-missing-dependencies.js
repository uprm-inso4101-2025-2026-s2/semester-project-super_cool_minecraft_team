import { getMissingDependencies } from './backend-missing-dependencies.js';

document.addEventListener("DOMContentLoaded", () => {
    missingDependenciesPage();
});

async function missingDependenciesPage() {
    const dependencyList = document.getElementById("dependency-list");
    const loadingState = document.getElementById("loading-state");
    const emptyState = document.getElementById("empty-state");
    const errorState = document.getElementById("error-state");
    const partialResultsBanner = document.getElementById("partial-results-banner");

    dependencyList.innerHTML = "";
    emptyState.hidden = true;
    errorState.hidden = true;
    partialResultsBanner.hidden = true;

    try {
        const data = await getMissingDependencies();

        if (data.error) {
            errorState.textContent = data.error;
            errorState.hidden = false;
            return;
        }
        
        const missingDependencies = Array.isArray(data.missingDependencies) ? data.missingDependencies : [];
        const resolvedById = new Map(
            (Array.isArray(data.resolvedDependencies) ? data.resolvedDependencies : [])
                .filter((dependency) => dependency && dependency.id)
                .map((dependency) => [dependency.id, dependency])
        );

        if (data.analysisHasPartialResults) {
            partialResultsBanner.hidden = false;
        }

        if (missingDependencies.length === 0) {
            emptyState.hidden = false;
            return;
        }

        for (const dependency of missingDependencies) {
            const resolvedDependency = resolvedById.get(dependency.id);
            dependencyList.appendChild(createDependencyCard(dependency, resolvedDependency));
        }
    } catch (error) {
        errorState.textContent = error.message || "Unable to load missing dependencies.";
        errorState.hidden = false;
    } finally {
        loadingState.hidden = true;
    }
}

function createDependencyCard(dependency, resolvedDependency) {
    const dependencyCard = document.createElement("div");
    dependencyCard.className = "dependency-item";

    const dependencyMain = document.createElement("div");
    dependencyMain.className = "dependency-main";

    const title = document.createElement("h3");
    title.textContent = dependency.name || dependency.id || "Unknown dependency";

    const details = document.createElement("p");
    details.className = "dependency-meta";
    details.textContent = [
        `Version: ${dependency.requiredVersion || "Unknown"}`,
        `Loader: ${dependency.loader || "Unknown"}`,
        `Minecraft: ${dependency.mcVersion || "Unknown"}`
    ].join(" | ");

    dependencyMain.appendChild(title);
    dependencyMain.appendChild(details);

    const dependencyActions = document.createElement("div");
    dependencyActions.className = "dependency-actions";

    const statusBadge = document.createElement("span");
    statusBadge.className = `status-badge ${resolvedDependency ? "status-resolved" : "status-unresolved"}`;
    statusBadge.textContent = resolvedDependency ? "Resolved" : "Not found";
    dependencyActions.appendChild(statusBadge);

    if (resolvedDependency && resolvedDependency.preferred) {
        const link = document.createElement("a");
        link.className = "download-btn";
        link.href = `/r?u=${encodeURIComponent(resolvedDependency.preferred)}`;
        link.target = "_blank";
        link.rel = "noopener noreferrer";
        link.textContent = "View Download Link";
        dependencyActions.appendChild(link);
    } else {
        const unresolvedMessage = document.createElement("p");
        unresolvedMessage.className = "status-message";
        unresolvedMessage.textContent = "No download link could be resolved for this dependency.";
        dependencyActions.appendChild(unresolvedMessage);
    }

    dependencyCard.appendChild(dependencyMain);
    dependencyCard.appendChild(dependencyActions);
    return dependencyCard;
}




