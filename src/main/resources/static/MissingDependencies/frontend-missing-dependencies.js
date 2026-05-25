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

        // for (const dependency of missingDependencies) {
        //     const resolvedDependency = resolvedById.get(dependency.id);
        //     dependencyList.appendChild(createDependencyCard(dependency, resolvedDependency));
        // }
        for (const dependency of missingDependencies) 
        {
            // If dependency is a string ID, use it directly; otherwise fallback to an object property
            const depId = typeof dependency === 'string' ? dependency : dependency.id;
            const resolvedDependency = resolvedById.get(depId);
            
            // Normalize dependency structure for the card creator
            const dependencyObj = typeof dependency === 'string' ? { id: dependency } : dependency;
            
            dependencyList.appendChild(createDependencyCard(dependencyObj, resolvedDependency));
        }


        const totalDependencies = missingDependencies.length;
        const resolvedDependencies = resolvedById.size;
        const unresolvedDependencies =
            totalDependencies - resolvedDependencies;

        const dependencySummary =
            document.getElementById("dependency-summary");

        document.getElementById("summary-total").textContent =
            totalDependencies;

        document.getElementById("summary-resolved").textContent =
            resolvedDependencies;

        document.getElementById("summary-unresolved").textContent =
            unresolvedDependencies;

        dependencySummary.hidden = false;



        
        setupDependencySearch();
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

function setupDependencySearch() {
    const searchInput = document.getElementById("dependency-search");
    const clearButton = document.getElementById("clear-search-button");
    const dependencyList = document.getElementById("dependency-list");

    if (!searchInput || !dependencyList) {
        return;
    }

    searchInput.addEventListener("input", () => {
        const searchValue = searchInput.value.toLowerCase();
        const dependencyCards = dependencyList.querySelectorAll(".dependency-item");

        dependencyCards.forEach((card) => {
            const dependencyText = card.textContent.toLowerCase();

            if (dependencyText.includes(searchValue)) {
                card.style.display = "flex";
            } else {
                card.style.display = "none";
            }
        });
    });

    clearButton.addEventListener("click", () => {
        searchInput.value = "";

        const dependencyCards =
            dependencyList.querySelectorAll(".dependency-item");

        dependencyCards.forEach((card) => {
            card.style.display = "flex";
        });

        searchInput.focus();
    });
}




