// javascript
let nodes = [];
let links = [];

const exampleData = {
    nodes: [
        { id: "CoreMod", type: "root" },
        { id: "JEI", type: "mod" },
        { id: "Optifine", type: "mod" },
        { id: "OldPhysics", type: "mod" },
        { id: "Patchouli", type: "mod" },
        { id: "Botania", type: "mod" }
    ],
    links: [
        { source: "CoreMod", target: "JEI", rel: "required" },
        { source: "CoreMod", target: "Optifine", rel: "optional" },
        { source: "CoreMod", target: "OldPhysics", rel: "conflict" },
        { source: "CoreMod", target: "Patchouli", rel: "required" },
        { source: "Patchouli", target: "Botania", rel: "required" }
    ]
};

const relColors = {
    required: "var(--blue)",
    optional: "var(--yellow)",
    conflict: "var(--red)"
};

const container = document.getElementById("dependency-graph-canvas");

function initGraph() {
    const width = container.offsetWidth;
    const height = container.offsetHeight;

    const svg = d3.select("#dependency-graph-canvas").selectAll("svg").data([null]).join("svg")
        .attr("width", "100%")
        .attr("height", "100%");

    const mainGroup = svg.selectAll("g.main").data([null]).join("g").attr("class", "main");

    svg.call(
        d3.zoom()
            .scaleExtent([0.2, 5])
            .on("zoom", (event) => {
                mainGroup.attr("transform", event.transform);
            })
    );

    const simulation = d3.forceSimulation(nodes)
        .force("link", d3.forceLink(links).id(d => d.id).distance(150))
        .force("charge", d3.forceManyBody().strength(-500))
        .force("center", d3.forceCenter(width / 2, height / 2));

    const link = mainGroup.append("g")
        .attr("class", "links")
        .selectAll("line")
        .data(links)
        .join("line")
        .attr("class", "link")
        .attr("stroke", d => relColors[d.rel]);

    const node = mainGroup.append("g")
        .attr("class", "nodes")
        .selectAll(".node")
        .data(nodes)
        .join("g")
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

    function showPanel(d) {
        const panel = document.getElementById("mod-info-panel");
        const title = document.getElementById("mod-title");
        const list = document.getElementById("mod-deps");

        if (!panel) return;
        panel.style.display = "flex";
        if (title) title.textContent = d.id;
        if (list) list.innerHTML = "";

        const connected = new Set();

        links.forEach(l => {
            const sId = typeof l.source === "object" ? l.source.id : l.source;
            const tId = typeof l.target === "object" ? l.target.id : l.target;
            if (sId === d.id || tId === d.id) {
                connected.add(sId);
                connected.add(tId);
            }
        });

        d3.selectAll(".node").classed("faded", true);
        d3.selectAll(".link").classed("faded", true);

        d3.selectAll(".node")
            .filter(n => connected.has(n.id))
            .classed("faded", false);

        d3.selectAll(".link")
            .filter(l => {
                const sId = typeof l.source === "object" ? l.source.id : l.source;
                const tId = typeof l.target === "object" ? l.target.id : l.target;
                return sId === d.id || tId === d.id;
            })
            .classed("faded", false);

        const connectedLinks = links.filter(l => {
            const sId = typeof l.source === "object" ? l.source.id : l.source;
            const tId = typeof l.target === "object" ? l.target.id : l.target;
            return sId === d.id || tId === d.id;
        });

        connectedLinks.forEach(l => {
            const neighbor = ((typeof l.source === "object" ? l.source.id : l.source) === d.id)
                ? (typeof l.target === "object" ? l.target.id : l.target)
                : (typeof l.source === "object" ? l.source.id : l.source);

            const li = document.createElement("li");
            li.textContent = `${l.rel.toUpperCase()}: ${neighbor}`;
            li.classList.add(`dep-${l.rel}`);
            if (list) list.appendChild(li);
        });
    }

    function closePanel() {
        const panel = document.getElementById("mod-info-panel");
        if (panel) panel.style.display = "none";

        d3.selectAll(".node").classed("faded", false);
        d3.selectAll(".link").classed("faded", false);
    }

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
}

// fetch data and initialize the graph; on error use exampleData
fetch("/api/graph")
    .then(res => {
        if (!res.ok) throw new Error("Network response was not ok");
        return res.json();
    })
    .then(data => {
        nodes = data && data.nodes ? data.nodes : exampleData.nodes;
        links = data && data.links ? data.links : exampleData.links;
        initGraph();
    })
    .catch(err => {
        console.warn("Failed to load graph data, using example data:", err);
        nodes = exampleData.nodes;
        links = exampleData.links;
        initGraph();
    });
