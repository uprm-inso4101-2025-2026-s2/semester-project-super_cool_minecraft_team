console.log("graph_visualization.js cargó");

/* ===== GRAPH DATA ===== */
const nodes = [
    { id: "CoreMod", type: "root", status: "compatible" },
    { id: "JEI", type: "mod", status: "compatible" },
    { id: "Optifine", type: "mod", status: "compatible" },
    { id: "OldPhysics", type: "mod", status: "incompatible" },
    { id: "Patchouli", type: "mod", status: "compatible" },
    { id: "Botania", type: "mod", status: "compatible" }
];

const links = [
    { source: "CoreMod", target: "JEI", rel: "required" },
    { source: "CoreMod", target: "Optifine", rel: "optional" },
    { source: "CoreMod", target: "OldPhysics", rel: "conflict" },
    { source: "CoreMod", target: "Patchouli", rel: "required" },
    { source: "Patchouli", target: "Botania", rel: "required" }
];

const relColors = {
    required: "var(--blue)",
    optional: "var(--yellow)",
    conflict: "var(--red)"
};

/* ===== INITIALIZE GRAPH DATA ===== */
// Load the mock data into the global graphData object from dependency-resolution.js
loadGraphData({
    nodes: nodes,
    links: links
});

/* ===== STATE TRACKING ===== */
let selectedNodeId = null;
let highlightState = {
    selectedNode: null,
    directDependencies: new Set(),
    directDependents: new Set(),
    allRelated: new Set()
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

/* ===== SVG + ZOOM ===== */
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

/* ===== FORCE SIMULATION ===== */
const simulation = d3.forceSimulation(nodes)
    .force("link", d3.forceLink(links).id((d) => d.id).distance(150))
    .force("charge", d3.forceManyBody().strength(-500))
    .force("center", d3.forceCenter(width / 2, height / 2));

/* ===== LINKS ===== */
const link = mainGroup.append("g")
    .selectAll("line")
    .data(links)
    .enter()
    .append("line")
    .attr("class", "link")
    .attr("stroke", (d) => relColors[d.rel] || "white");

/* ===== NODES ===== */
const node = mainGroup.append("g")
    .selectAll(".node")
    .data(nodes)
    .enter()
    .append("g")
    .attr("class", "node")
    .on("click", (event, d) => handleNodeClick(event, d))
    .call(
        d3.drag()
            .on("start", dragstarted)
            .on("drag", dragged)
            .on("end", dragended)
    );

node.append("circle")
    .attr("r", 10)
    .attr("fill", (d) => d.type === "root" ? "var(--blue)" : "var(--panel)")
    .attr("class", "node-circle");

node.append("text")
    .attr("dx", 14)
    .attr("dy", ".35em")
    .attr("class", "node-text")
    .text((d) => d.id);

/* ===== TICK ===== */
simulation.on("tick", () => {
    link
        .attr("x1", (d) => d.source.x)
        .attr("y1", (d) => d.source.y)
        .attr("x2", (d) => d.target.x)
        .attr("y2", (d) => d.target.y);

    node.attr("transform", (d) => `translate(${d.x},${d.y})`);
});

/* ===== HELPERS ===== */
function getNodeId(endpoint) {
    return typeof endpoint === "object" ? endpoint.id : endpoint;
}

function buildHighlightState(selectedId) {
    const dependencies = new Set(getDependencies(selectedId));
    const dependents = new Set(getDependents(selectedId));
    
    // All related nodes: selected + direct dependencies + dependents
    const allRelated = new Set([selectedId, ...dependencies, ...dependents]);
    
    return {
        selectedNode: selectedId,
        directDependencies: dependencies,
        directDependents: dependents,
        allRelated: allRelated
    };
}

function getNodeStateClass(nodeData, state) {
    if (!state.selectedNode) return "normal";
    
    if (nodeData.id === state.selectedNode) {
        return "selected";
    }
    
    if (state.directDependencies.has(nodeData.id)) {
        return "dependency";
    }
    
    if (state.directDependents.has(nodeData.id)) {
        return "dependent";
    }
    
    return "dimmed";
}

function getLinkStateClass(linkData, state) {
    if (!state.selectedNode) return "normal";
    
    const sourceId = getNodeId(linkData.source);
    const targetId = getNodeId(linkData.target);
    
    // Link is related if it connects to any related node
    const isRelated = state.allRelated.has(sourceId) && state.allRelated.has(targetId);
    
    return isRelated ? "highlighted" : "dimmed";
}

/* ===== PANEL LOGIC ===== */
function showPanel(d) {
    if (!panel || !title || !status || !list) return;

    // Update highlight state
    highlightState = buildHighlightState(d.id);
    selectedNodeId = d.id;

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

    // Apply dynamic styling to nodes
    d3.selectAll(".node").each(function(nodeData) {
        const stateClass = getNodeStateClass(nodeData, highlightState);
        d3.select(this).attr("class", `node ${stateClass}`);
    });

    // Apply dynamic styling to links
    d3.selectAll(".link").each(function(linkData) {
        const stateClass = getLinkStateClass(linkData, highlightState);
        d3.select(this).attr("class", `link ${stateClass}`);
    });

    // Apply animation to selected node
    d3.selectAll(".node-circle").filter((n) => n.id === d.id).classed("pulse", true);

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

    // Reset state
    selectedNodeId = null;
    highlightState = {
        selectedNode: null,
        directDependencies: new Set(),
        directDependents: new Set(),
        allRelated: new Set()
    };

    d3.selectAll(".node").classed("faded", false);
    d3.selectAll(".link").classed("faded", false);

    // Reset node styling
    d3.selectAll(".node").attr("class", "node normal");
    d3.selectAll(".node-circle").classed("pulse", false);

    // Reset link styling
    d3.selectAll(".link").attr("class", "link normal");
}

window.closePanel = closePanel;

/* ===== BUTTON EVENTS ===== */
if (closePanelBtn) {
    closePanelBtn.addEventListener("click", closePanel);
}

if (zoomInBtn) {
    zoomInBtn.addEventListener("click", () => zoomBy(1.2));
}

if (zoomOutBtn) {
    zoomOutBtn.addEventListener("click", () => zoomBy(0.8));
}

if (resetViewBtn) {
    resetViewBtn.addEventListener("click", resetView);
}

function handleNodeClick(event, d) {
    event.stopPropagation();
    
    // Toggle: clicking same node twice clears selection
    if (selectedNodeId === d.id) {
        closePanel();
        return;
    }
    
    selectedNodeId = d.id;
    showPanel(d);
}

/* ===== RESET ON CANVAS CLICK ===== */
// Clicking empty canvas (outside nodes) resets highlighting
svg.on("click", function(event) {
    // Only reset if clicking on the SVG background, not on a node
    if (event.target === this) {
        closePanel();
    }
});

/* ===== DRAG ===== */
function dragstarted(event, d) {
    if (!event.active) simulation.alphaTarget(0.3).restart();
    d.fx = d.x;
    d.fy = d.y;
}

function dragged(event, d) {
    d.fx = event.x;
    d.fy = event.y;
}

function dragended(event, d) {
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
