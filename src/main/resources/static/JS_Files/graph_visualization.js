/* ===== GRAPH RENDERING LOGIC ===== */
const nodes = [
    { id: "CoreMod", type: "root", status: "compatible" },
    { id: "JEI", type: "mod", status: "compatible" },
    { id: "Optifine", type: "mod", status: "compatible" },
    { id: "OldPhysics", type: "mod", status: "incompatible" },
    { id: "Patchouli", type: "mod", status: "compatible" },
    { id: "Botania", type: "mod", status: "compatible" },
];

const links = [
    { source: "CoreMod", target: "JEI", rel: "required" },
    { source: "CoreMod", target: "Optifine", rel: "optional" },
    { source: "CoreMod", target: "OldPhysics", rel: "conflict" },
    { source: "CoreMod", target: "Patchouli", rel: "required" },
    { source: "Patchouli", target: "Botania", rel: "required" },
];

const relColors = {
    required: "var(--blue)",
    optional: "var(--yellow)",
    conflict: "var(--red)"
};

const container = document.getElementById("dependency-graph-canvas");
const width = container.offsetWidth;
const height = container.offsetHeight;
let simulation = null;

/* ===== JSON VALIDATION LOGIC ===== */

function validateGraphData(nodes, links) {
    const errors = [];
    const warnings = [];

    if (!Array.isArray(nodes)) {
        errors.push("Nodes data is not an array");
        return { isValid: false, errors, warnings, nodeIds: new Set() };
    }

    if (nodes.length === 0) {
        errors.push("No nodes found in graph data");
        return { isValid: false, errors, warnings, nodeIds: new Set() };
    }

    const nodeIds = new Set();

    nodes.forEach((node, index) => {
        if (!node) {
            errors.push(`Node at index ${index} is null or undefined`);
            return;
        }
        if (!node.id) {
            errors.push(`Node at index ${index} is missing required 'id' property`);
        } else {
            nodeIds.add(node.id);
        }
        if (!node.type) {
            errors.push(`Node '${node.id || `index ${index}`}' is missing required 'type' property`);
        }
        if (!node.status) {
            errors.push(`Node '${node.id || `index ${index}`}' is missing required 'status' property`);
        }
    });

    if (!Array.isArray(links)) {
        errors.push("Links data is not an array");
        return { isValid: false, errors, warnings, nodeIds };
    }

    const missingDependencies = identifyMissingDependencies(links, nodeIds);

    if (missingDependencies.size > 0) {
        missingDependencies.forEach(depId => {
            warnings.push(`Missing dependency detected: '${depId}' is referenced but not found in nodes`);
            console.warn(`Missing dependency: '${depId}'`);
        });
    }

    links.forEach((link, index) => {
        if (!link) {
            errors.push(`Link at index ${index} is null or undefined`);
            return;
        }
        if (!link.source) {
            errors.push(`Link at index ${index} is missing required 'source' property`);
        }
        if (!link.target) {
            errors.push(`Link at index ${index} is missing required 'target' property`);
        }
        if (!link.rel) {
            errors.push(`Link at index ${index} is missing required 'rel' property`);
        } else if (!["required", "optional", "conflict"].includes(link.rel)) {
            errors.push(`Link at index ${index} has invalid 'rel' value: '${link.rel}'`);
        }
    });

    const isValid = errors.length === 0;
    return { isValid, errors, warnings, nodeIds, missingDependencies };
}

function identifyMissingDependencies(links, nodeIds) {
    const missingDependencies = new Set();
    links.forEach(link => {
        if (link.source && !nodeIds.has(link.source)) missingDependencies.add(link.source);
        if (link.target && !nodeIds.has(link.target)) missingDependencies.add(link.target);
    });
    return missingDependencies;
}

function showErrorBanner(errors, warnings) {
    const banner = document.getElementById("error-warning-banner");
    const errorContent = document.getElementById("error-banner-content");
    const closeButton = document.getElementById("error-banner-close");

    if (!banner || !errorContent) {
        console.error("Error banner elements not found in DOM");
        return;
    }

    let bannerHTML = "";

    if (errors.length > 0) {
        bannerHTML += "<strong style='color: var(--red);'>⚠ Errors:</strong><ul>";
        errors.forEach(error => {
            bannerHTML += `<li>${escapeHtml(error)}</li>`;
            console.error(`Validation error: ${error}`);
        });
        bannerHTML += "</ul>";
    }

    if (warnings.length > 0) {
        bannerHTML += "<strong style='color: var(--yellow);'>⚠ Warnings:</strong><ul>";
        warnings.forEach(warning => {
            bannerHTML += `<li>${escapeHtml(warning)}</li>`;
        });
        bannerHTML += "</ul>";
    }

    errorContent.innerHTML = bannerHTML;
    banner.style.display = "flex";

    closeButton.addEventListener("click", () => {
        banner.style.display = "none";
    });
}

function addWarningBadges(missingDependencies) {
    if (!missingDependencies || missingDependencies.size === 0) return;

    const nodeElements = d3.selectAll(".node");
    nodeElements.each(function(d) {
        if (missingDependencies.has(d.id)) {
            const selection = d3.select(this);
            selection.append("text")
                .attr("class", "warning-badge")
                .attr("dx", -12)
                .attr("dy", -12)
                .attr("text-anchor", "middle")
                .attr("font-size", "14")
                .attr("fill", "var(--yellow)")
                .attr("font-weight", "bold")
                .text("⚠")
                .style("pointer-events", "none")
                .style("animation", "pulse-warning 1.5s infinite");
        }
    });
}

function escapeHtml(text) {
    const div = document.createElement("div");
    div.textContent = text;
    return div.innerHTML;
}

function showCriticalErrorMessage() {
    const container = document.getElementById("dependency-graph-canvas");
    container.innerHTML = "";

    const errorDiv = document.createElement("div");
    errorDiv.style.cssText = `
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        height: 100%;
        padding: 40px;
        text-align: center;
        color: var(--red);
    `;

    errorDiv.innerHTML = `
        <h2 style="font-size: 1.5rem; margin-bottom: 16px; color: var(--red);">⚠ Unable to Render Graph</h2>
        <p style="font-size: 1rem; color: var(--text); margin-bottom: 16px;">Critical errors were found in the graph data. Please fix the errors shown above and reload.</p>
    `;

    container.appendChild(errorDiv);
}

/* ===== GRAPH INITIALIZATION AND RENDERING ===== */

const validationResult = validateGraphData(nodes, links);

if (!validationResult.isValid) {
    console.error("Graph data validation failed. Cannot render graph.");
    showErrorBanner(validationResult.errors, []);
    showCriticalErrorMessage();
} else {
    const renderableLinks = links.filter(link => {
        return validationResult.nodeIds.has(link.source) && validationResult.nodeIds.has(link.target);
    });

    const svg = d3.select("#dependency-graph-canvas")
        .append("svg")
        .attr("width", "100%")
        .attr("height", height);

    const mainGroup = svg.append("g");

    const zoom = d3.zoom()
        .scaleExtent([0.2, 5])
        .on("zoom", (event) => {
            mainGroup.attr("transform", event.transform);
        });
    svg.call(zoom);

    const resetViewBtn = document.getElementById("resetViewBtn");
    resetViewBtn.addEventListener("click", () => {
        svg.transition()
            .duration(500)
            .call(zoom.transform, d3.zoomIdentity);
    });

    simulation = d3.forceSimulation(nodes)
        .force("link", d3.forceLink(renderableLinks).id(d => d.id).distance(150))
        .force("charge", d3.forceManyBody().strength(-500))
        .force("center", d3.forceCenter(width / 2, height / 2));

    const link = mainGroup.append("g")
        .selectAll("line")
        .data(renderableLinks)
        .enter()
        .append("line")
        .attr("class", "link")
        .attr("stroke", d => relColors[d.rel]);

    const node = mainGroup.append("g")
        .selectAll(".node")
        .data(nodes)
        .enter()
        .append("g")
        .attr("class", "node")
        .on("click", (event, d) => showPanel(d))
        .call(
            d3.drag()
                .on("start", dragstarted)
                .on("drag", dragged)
                .on("end", dragended)
        );

    node.append("circle")
        .attr("r", 10)
        .attr("fill", d => d.type === "root" ? "var(--blue)" : "var(--panel)");

    node.append("text")
        .attr("dx", 14)
        .attr("dy", ".35em")
        .text(d => d.id);

    if (validationResult.missingDependencies) {
        addWarningBadges(validationResult.missingDependencies);
    }

    simulation.on("tick", () => {
        link
            .attr("x1", d => d.source.x)
            .attr("y1", d => d.source.y)
            .attr("x2", d => d.target.x)
            .attr("y2", d => d.target.y);

        node.attr("transform", d => `translate(${d.x},${d.y})`);
    });

    if (validationResult.warnings.length > 0) {
        showErrorBanner([], validationResult.warnings);
    }
}

/* ===== PANEL + FADE LOGIC ===== */

function showPanel(d) {
    const panel = document.getElementById("mod-info-panel");
    const title = document.getElementById("mod-title");
    const status = document.getElementById("mod-conflict-status");
    const list = document.getElementById("mod-deps");

    panel.style.display = "flex";
    title.textContent = d.id;

    const statusText = d.status === "incompatible" ? "Conflict" : "Compatible";
    status.textContent = statusText;
    status.classList.remove("status-compatible", "status-incompatible");
    status.classList.add(d.status === "incompatible" ? "status-incompatible" : "status-compatible");

    list.innerHTML = "";

    const connected = new Set();
    links.forEach(l => {
        if (l.source.id === d.id || l.target.id === d.id) {
            connected.add(l.source.id);
            connected.add(l.target.id);
        }
    });

    d3.selectAll(".node").classed("faded", true);
    d3.selectAll(".link").classed("faded", true);

    d3.selectAll(".node").filter(n => connected.has(n.id)).classed("faded", false);
    d3.selectAll(".link").filter(l => l.source.id === d.id || l.target.id === d.id).classed("faded", false);

    const connectedLinks = links.filter(l => l.source.id === d.id || l.target.id === d.id);
    connectedLinks.forEach(l => {
        const neighbor = l.source.id === d.id ? l.target.id : l.source.id;
        const li = document.createElement("li");
        li.textContent = `${l.rel.toUpperCase()}: ${neighbor}`;
        li.classList.add(`dep-${l.rel}`);
        list.appendChild(li);
    });
}

function closePanel() {
    document.getElementById("mod-info-panel").style.display = "none";
    d3.selectAll(".node").classed("faded", false);
    d3.selectAll(".link").classed("faded", false);
}

/* ===== DRAG ===== */

function dragstarted(event, d) {
    if (!simulation) return;
    if (!event.active) simulation.alphaTarget(0.3).restart();
    d.fx = d.x;
    d.fy = d.y;
}

function dragged(event, d) {
    d.fx = event.x;
    d.fy = event.y;
}

function dragended(event, d) {
    if (!simulation) return;
    if (!event.active) simulation.alphaTarget(0);
    d.fx = null;
    d.fy = null;
}