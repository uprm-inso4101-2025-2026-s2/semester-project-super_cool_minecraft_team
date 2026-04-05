console.log("graph_visualization.js cargó");

/* ===== GRAPH DATA ===== */
const rawGraphData = window.graphData || { nodes: [], links: [] };
const nodes = (rawGraphData.nodes || []).map(node => ({
    id: node.id,
    type: node.type || "mod",
    status: node.status || "compatible"
}));

const links = (rawGraphData.links || []).map(link => ({
    source: link.source,
    target: link.target,
    rel: link.rel || "required"
}));

if (nodes.length === 0) {
    console.warn("No graph data available");
}
if (links.length === 0) {
    console.warn("No graph links available");
}

const relColors = {
    required: "var(--blue)",
    optional: "var(--yellow)",
    conflict: "var(--red)"
};

/* ===== DOM REFERENCES ===== */
const container = document.getElementById("dependency-graph-canvas");
const panel = document.getElementById("mod-info-panel");
const title = document.getElementById("mod-title");
const status = document.getElementById("mod-conflict-status");
const list = document.getElementById("mod-deps");

const closePanelBtn = document.getElementById("closePanelBtn");
const zoomInBtn = document.getElementById("zoomInBtn");
const zoomOutBtn = document.getElementById("zoomOutBtn");
const resetViewBtn = document.getElementById("resetViewBtn");

if (!container) {
    throw new Error('No se encontró el elemento #dependency-graph-canvas');
}

/* ===== DIMENSIONS ===== */
const width = container.clientWidth || 800;
const height = container.clientHeight || 650;
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
        .attr("width", width)
        .attr("height", height)
        .attr("viewBox", `0 0 ${width} ${height}`);

    const mainGroup = svg.append("g");

    const zoom = d3.zoom()
        .scaleExtent([0.2, 5])
        .on("zoom", (event) => {
            mainGroup.attr("transform", event.transform);
        });
    svg.call(zoom);

    function zoomBy(factor) {
        svg.transition()
            .duration(250)
            .call(zoom.scaleBy, factor);
    }

    function resetView() {
        svg.transition()
            .duration(500)
            .call(zoom.transform, d3.zoomIdentity);
    }

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
        .attr("stroke", d => relColors[d.rel] || "white");

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

    if (zoomInBtn) zoomInBtn.addEventListener("click", () => zoomBy(1.2));
    if (zoomOutBtn) zoomOutBtn.addEventListener("click", () => zoomBy(0.8));
    if (resetViewBtn) resetViewBtn.addEventListener("click", resetView);
}

/* ===== HELPERS ===== */
function getNodeId(endpoint) {
    return typeof endpoint === "object" ? endpoint.id : endpoint;
}

/* ===== PANEL + FADE LOGIC ===== */
function showPanel(d) {
    if (!panel || !title || !status || !list) return;

    panel.style.display = "flex";
    title.textContent = d.id;

    const statusText = d.status === "incompatible" ? "Conflict" : "Compatible";
    status.textContent = statusText;
    status.classList.remove("status-compatible", "status-incompatible");
    status.classList.add(
        d.status === "incompatible" ? "status-incompatible" : "status-compatible"
    );

    list.innerHTML = "";

    const connected = new Set();

    links.forEach((l) => {
        const sourceId = getNodeId(l.source);
        const targetId = getNodeId(l.target);

        if (sourceId === d.id || targetId === d.id) {
            connected.add(sourceId);
            connected.add(targetId);
        }
    });

    d3.selectAll(".node").classed("faded", true);
    d3.selectAll(".link").classed("faded", true);

    d3.selectAll(".node")
        .filter((n) => connected.has(n.id))
        .classed("faded", false);

    d3.selectAll(".link")
        .filter((l) => {
            const sourceId = getNodeId(l.source);
            const targetId = getNodeId(l.target);
            return sourceId === d.id || targetId === d.id;
        })
        .classed("faded", false);

    const connectedLinks = links.filter((l) => {
        const sourceId = getNodeId(l.source);
        const targetId = getNodeId(l.target);
        return sourceId === d.id || targetId === d.id;
    });

    connectedLinks.forEach((l) => {
        const sourceId = getNodeId(l.source);
        const targetId = getNodeId(l.target);
        const neighbor = sourceId === d.id ? targetId : sourceId;

        const li = document.createElement("li");
        li.textContent = `${l.rel.toUpperCase()}: ${neighbor}`;
        li.classList.add(`dep-${l.rel}`);
        list.appendChild(li);
    });
}

function closePanel() {
    if (panel) {
        panel.style.display = "none";
    }
    d3.selectAll(".node").classed("faded", false);
    d3.selectAll(".link").classed("faded", false);
}

window.closePanel = closePanel;

/* ===== BUTTON EVENTS ===== */
if (closePanelBtn) {
    closePanelBtn.addEventListener("click", closePanel);
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

/* ===== SEARCH LOGIC + NODE MATCHING ===== */

function normalizeSearchValue(value) {
    return (value || "").trim().toLowerCase();
}

function getNodeSearchLabel(nodeData) {
    if (!nodeData) return "";
    return normalizeSearchValue(nodeData.id);
}

function findNodeByQuery(nodeList, query) {
    const normalizedQuery = normalizeSearchValue(query);

    if (!normalizedQuery || !Array.isArray(nodeList)) {
        return null;
    }

    const exactMatch = nodeList.find(
        (nodeData) => getNodeSearchLabel(nodeData) === normalizedQuery
    );

    if (exactMatch) {
        return exactMatch;
    }

    return (
        nodeList.find((nodeData) =>
            getNodeSearchLabel(nodeData).includes(normalizedQuery)
        ) || null
    );
}

function getMatchingNodes(nodeList, query) {
    const normalizedQuery = normalizeSearchValue(query);

    if (!normalizedQuery || !Array.isArray(nodeList)) {
        return [];
    }

    return nodeList.filter((nodeData) =>
        getNodeSearchLabel(nodeData).includes(normalizedQuery)
    );
}