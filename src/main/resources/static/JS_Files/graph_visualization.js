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