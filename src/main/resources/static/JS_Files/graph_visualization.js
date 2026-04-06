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

/* ===== INITIALIZE GRAPH DATA ===== */
// Load the data into the global graphData object from dependency-resolution.js
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

    /* ===== PAN BOUNDS CONFIGURATION ===== */
    const PAN_BOUNDS = {
        maxPanX: 400,
        maxPanY: 400
    };

    const zoom = d3.zoom()
        .scaleExtent([0.2, 5])
        .on("zoom", (event) => {
            let transform = event.transform;

            const constrainedX = Math.max(-PAN_BOUNDS.maxPanX, Math.min(PAN_BOUNDS.maxPanX, transform.x));
            const constrainedY = Math.max(-PAN_BOUNDS.maxPanY, Math.min(PAN_BOUNDS.maxPanY, transform.y));

            transform = d3.zoomIdentity
                .translate(constrainedX, constrainedY)
                .scale(transform.k);

            mainGroup.attr("transform", transform);
        });

    let isPanning = false;

    svg.on("mousedown", function() {
        isPanning = true;
        svg.style("cursor", "grabbing");
    })
    .on("mouseup", function() {
        isPanning = false;
        svg.style("cursor", "grab");
    })
    .on("mouseleave", function() {
        isPanning = false;
        svg.style("cursor", "grab");
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
        .on("click", (event, d) => handleNodeClick(event, d))
        .call(
            d3.drag()
                .on("start", dragstarted)
                .on("drag", dragged)
                .on("end", dragended)
        );

    node.append("circle")
        .attr("r", 10)
        .attr("fill", d => d.type === "root" ? "var(--blue)" : "var(--panel)")
        .attr("class", "node-circle");

    node.append("text")
        .attr("dx", 14)
        .attr("dy", ".35em")
        .attr("class", "node-text")
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

    node.on('click', (event, d) => {
        if (searchInput) searchInput.value = '';
        if (clearBtn) clearBtn.style.display = 'none';
        if (searchResults) searchResults.classList.remove('show');
        currentSelectedNodeId = null;
        event.stopPropagation();
    });

    /* ===== RESET ON CANVAS CLICK ===== */
    // Clicking empty canvas (outside nodes) resets highlighting
    svg.on("click", function(event) {
        // Only reset if clicking on the SVG background, not on a node
        if (event.target === this) {
            closePanel();
        }
    });
}

/* ===== HELPERS ===== */
function getNodeId(endpoint) {
    return typeof endpoint === "object" ? endpoint.id : endpoint;
}

function buildHighlightState(selectedId) {
    // Call the functions from dependency-resolution.js
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

/* ===== PANEL + FADE LOGIC ===== */
function showPanel(d) {
    if (!panel || !title || !status || !list) return;

    // Update highlight state - uses getDependencies/getDependents from dependency-resolution.js
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
        const direction = sourceId === d.id ? "DEPENDENCY" : "DEPENDENT";

        const li = document.createElement("li");
        li.textContent = `${direction}: ${neighbor}`;
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

/**
 * Handles node click events with toggle functionality
 * Clicking the same node twice will toggle highlighting on/off
 * @param {Event} event - Click event
 * @param {Object} d - Node data
 */
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

/* ===== SEARCH AND FOCUS FEATURE ===== */
const searchInput = document.getElementById('mod-search-input');
const clearBtn = document.getElementById('search-clear-btn');
const searchResults = document.getElementById('search-results');

let currentSelectedNodeId = null;

function getNodeList() {
    return nodes || [];
}

function updateSearchResults(searchTerm) {
    if (!searchTerm || searchTerm.trim() === '') {
        searchResults.classList.remove('show');
        return;
    }

    const matchingNodes = getMatchingNodes(getNodeList(), searchTerm);

    if (matchingNodes.length === 0) {
        searchResults.classList.remove('show');
        return;
    }

    searchResults.innerHTML = '';
    matchingNodes.forEach(node => {
        const item = document.createElement('div');
        item.className = 'search-result-item';
        item.textContent = node.id;
        item.setAttribute('role', 'option');
        item.onclick = () => {
            selectNode(node.id);
            searchInput.value = node.id;
            searchResults.classList.remove('show');
            clearBtn.style.display = 'block';
        };
        searchResults.appendChild(item);
    });

    searchResults.classList.add('show');
}

function highlightNode(nodeId) {
    d3.selectAll('.node').classed('highlighted', false);
    d3.selectAll('.node')
        .filter(d => d.id === nodeId)
        .classed('highlighted', true);
}

function zoomToNode(nodeId) {
    const nodeData = getNodeList().find(n => n.id === nodeId);
    if (!nodeData || nodeData.x === undefined || nodeData.y === undefined) {
        console.warn(`Node ${nodeId} not found or has no position data`);
        return;
    }

    const containerWidth = container.clientWidth || 800;
    const containerHeight = container.clientHeight || 650;
    const targetScale = 1.5;
    const translateX = containerWidth / 2 - nodeData.x * targetScale;
    const translateY = containerHeight / 2 - nodeData.y * targetScale;

    svg.transition()
        .duration(500)
        .call(
            zoom.transform,
            d3.zoomIdentity
                .translate(translateX, translateY)
                .scale(targetScale)
        );
}

function dimUnrelatedNodes(selectedNodeId) {
    if (!selectedNodeId) {
        d3.selectAll('.node').classed('faded', false);
        d3.selectAll('.link').classed('faded', false);
        return;
    }

    const connectedNodes = new Set([selectedNodeId]);

    links.forEach(link => {
        const sourceId = getNodeId(link.source);
        const targetId = getNodeId(link.target);

        if (sourceId === selectedNodeId) connectedNodes.add(targetId);
        if (targetId === selectedNodeId) connectedNodes.add(sourceId);
    });

    d3.selectAll('.node')
        .filter(d => !connectedNodes.has(d.id))
        .classed('faded', true);

    d3.selectAll('.node')
        .filter(d => d.id === selectedNodeId)
        .classed('faded', false);

    d3.selectAll('.link')
        .filter(link => {
            const sourceId = getNodeId(link.source);
            const targetId = getNodeId(link.target);
            return sourceId !== selectedNodeId && targetId !== selectedNodeId;
        })
        .classed('faded', true);
}

function resetVisuals() {
    d3.selectAll('.node').classed('highlighted', false);
    d3.selectAll('.node').classed('faded', false);
    d3.selectAll('.link').classed('faded', false);
    currentSelectedNodeId = null;

    svg.transition()
        .duration(300)
        .call(zoom.transform, d3.zoomIdentity);
}

function selectNode(nodeId) {
    const nodeExists = getNodeList().some(n => n.id === nodeId);
    if (!nodeExists) {
        console.warn(`Node ${nodeId} does not exist`);
        return;
    }

    currentSelectedNodeId = nodeId;
    highlightNode(nodeId);
    zoomToNode(nodeId);
    dimUnrelatedNodes(nodeId);
}

function clearSearch() {
    searchInput.value = '';
    clearBtn.style.display = 'none';
    searchResults.classList.remove('show');
    resetVisuals();
}

function handleSearchInput(e) {
    const searchTerm = e.target.value;

    if (!searchTerm || searchTerm.trim() === '') {
        clearSearch();
        return;
    }

    clearBtn.style.display = 'block';
    updateSearchResults(searchTerm);

    const exactMatch = findNodeByQuery(getNodeList(), searchTerm);
    if (exactMatch) selectNode(exactMatch.id);
}

function handleSearchKeypress(e) {
    if (e.key === 'Enter' && searchInput.value.trim()) {
        const exactMatch = findNodeByQuery(getNodeList(), searchInput.value);
        if (exactMatch) {
            selectNode(exactMatch.id);
            searchResults.classList.remove('show');
        }
    }
}

if (searchInput) {
    searchInput.addEventListener('input', handleSearchInput);
    searchInput.addEventListener('keypress', handleSearchKeypress);
}

if (clearBtn) {
    clearBtn.addEventListener('click', clearSearch);
}

document.addEventListener('click', (e) => {
    if (!searchInput || !searchResults) return;
    if (!searchInput.contains(e.target) && !searchResults.contains(e.target)) {
        searchResults.classList.remove('show');
    }
});

const originalShowPanel = showPanel || (() => {});
window.showPanel = function(d) {
    originalShowPanel(d);
    if (searchInput && d && d.id) {
        searchInput.value = d.id;
        clearBtn.style.display = 'block';
        selectNode(d.id);
    }
};