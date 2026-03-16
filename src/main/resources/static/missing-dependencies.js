document.addEventListener("DOMContentLoaded", () => {
    missingDependenciesPage();
    console.log("Missing dependencies page loaded");

});


async function missingDependenciesPage(){
    console.log("Missing Dependencies page has been initialized");

    const dependencyList = document.getElementById("dependency-list");
    const loadingState= document.getElementById("loading-state");
    const emptyState= document.getElementById("empty-state");
    const partialResultsBanner= document.getElementById("partial-results-banner");
    
    console.log(dependencyList);
    console.log(loadingState);
    console.log(emptyState);
    console.log(partialResultsBanner);

    const data= await getMissingDependencies();
    console.log("Missing dependencies data:", data);

    if (data.analysisHasPartialResults){
        partialResultsBanner.hidden= false;
    }
    
    loadingState.hidden= true;

    if(!data.missingDependencies || data.missingDependencies.length ==0){
        emptyState.hidden = false;
        return;
    }

    for (const dependency of data.missingDependencies){

    const dependencyCard= document.createElement("div");
    dependencyCard.className= "dependency-item";

    const dependencyMain= document.createElement("div");
    dependencyMain.className= "dependency-main";

    const title= document.createElement("h3");
    title.textContent= dependency.name;

    const details= document.createElement("p");
    details.textContent= `Version: ${dependency.version || "Unknown"} | Loader: ${dependency.loader || "Unknown"}`;

    dependencyMain.appendChild(title);
    dependencyMain.appendChild(details);

    const dependencyActions= document.createElement("div");
    dependencyActions.className= "dependency-actions";

    if(dependency.resolved){
        const link= document.createElement("a");
        link.className= "download-btn";
        link.href= dependency.downloadUrl;
        link.target= "_blank";
        link.rel= "noopener noreferrer";
        link.textContent="View Download Link";
        dependencyActions.appendChild(link);
    }else{
        dependencyActions.classList.add("unresolved");
        const unresolvedBadge= document.createElement("span");
        unresolvedBadge.className= "unresolved-badge";
        unresolvedBadge.textContent= "Unresolved";
        
        const unresolvedMessage= document.createElement("p");
        unresolvedMessage.textContent= "No download link could be resolved for this dependency.";
        
        dependencyActions.appendChild(unresolvedBadge);
        dependencyActions.appendChild(unresolvedMessage);
    }

    dependencyCard.appendChild(dependencyMain);
    dependencyCard.appendChild(dependencyActions);
    dependencyList.appendChild(dependencyCard);

}
}

async function getMissingDependencies(){
    return {
        analysisHasPartialResults: true,
        missingDependencies : [
            {
                name: "Fabric API",
                version: "1.20.1",
                loader: "Fabric",
                resolved: true,
                downloadUrl: "https://modrinth.com/mod/fabric-api"
            },
            {
                name: "Cloth Config",
                version: "1.20.1",
                loader: "Fabric",
                resolved: true,
                downloadUrl: "https://modrinth.com/mod/cloth-config"
            },
            {
                name: "Mod Menu",
                version: "1.20.1",
                loader: "Fabric",
                resolved: false,
                downloadUrl: null
            }
        ]
    }
}
