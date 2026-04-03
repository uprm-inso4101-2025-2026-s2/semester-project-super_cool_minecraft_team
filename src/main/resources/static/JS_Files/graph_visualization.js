/* ===== GRAPH RENDERING LOGIC ===== */
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

const container = document.getElementById("dependency-graph-canvas");
const width = container.offsetWidth;
const height = container.offsetHeight;

/*Reset view logic*/
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



const simulation = d3.forceSimulation(nodes)
    .force("link", d3.forceLink(links).id(d => d.id).distance(150))
    .force("charge", d3.forceManyBody().strength(-500))
    .force("center", d3.forceCenter(width / 2, height / 2));

const link = mainGroup.append("g")
    .selectAll("line")
    .data(links)
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

simulation.on("tick", () => {
    link
        .attr("x1", d => d.source.x)
        .attr("y1", d => d.source.y)
        .attr("x2", d => d.target.x)
        .attr("y2", d => d.target.y);

    node.attr("transform", d => `translate(${d.x},${d.y})`);
});



/* ===== PANEL + FADE LOGIC ===== */

function showPanel(d) {
    const panel = document.getElementById("mod-info-panel");
    const title = document.getElementById("mod-title");
    const status = document.getElementById("mod-conflict-status");
    const list = document.getElementById("mod-deps");

    panel.style.display = "flex";
    title.textContent = d.id;
    
    // Set conflict status and color
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

    // Fade all
    d3.selectAll(".node").classed("faded", true);
    d3.selectAll(".link").classed("faded", true);

    // Highlight connected
    d3.selectAll(".node")
        .filter(n => connected.has(n.id))
        .classed("faded", false);

    d3.selectAll(".link")
        .filter(l => l.source.id === d.id || l.target.id === d.id)
        .classed("faded", false);

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
