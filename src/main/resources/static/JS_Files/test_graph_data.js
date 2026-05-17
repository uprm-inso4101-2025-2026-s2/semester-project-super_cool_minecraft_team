
(function() {
    // Simple helper that returns small sets of nodes/links for manual testing
    function getTestGraphData(mode = "small") {
        if (mode === "medium") {
            return {
                nodes: [
                    { id: "CoreMod", type: "root", status: "compatible" },
                    { id: "JEI", type: "mod", status: "compatible" },
                    { id: "Optifine", type: "mod", status: "compatible" },
                    { id: "OldPhysics", type: "mod", status: "incompatible" },
                    { id: "Patchouli", type: "mod", status: "compatible" },
                    { id: "Botania", type: "mod", status: "compatible" },
                    { id: "ExtraLib", type: "mod", status: "compatible" },
                    { id: "Renderer", type: "mod", status: "compatible" }
                ],
                links: [
                    { source: "CoreMod", target: "JEI", rel: "required" },
                    { source: "CoreMod", target: "Optifine", rel: "optional" },
                    { source: "CoreMod", target: "OldPhysics", rel: "conflict" },
                    { source: "CoreMod", target: "Patchouli", rel: "required" },
                    { source: "Patchouli", target: "Botania", rel: "required" },
                    { source: "ExtraLib", target: "Renderer", rel: "required" },
                    { source: "JEI", target: "Renderer", rel: "optional" }
                ]
            };
        }

        if (mode === "cycle") {
            return {
                nodes: [
                    { id: "A", type: "mod", status: "compatible" },
                    { id: "B", type: "mod", status: "compatible" },
                    { id: "C", type: "mod", status: "compatible" }
                ],
                links: [
                    { source: "A", target: "B", rel: "required" },
                    { source: "B", target: "C", rel: "required" },
                    { source: "C", target: "A", rel: "optional" }
                ]
            };
        }

        // default: small dataset
        return {
            nodes: [
                { id: "CoreMod", type: "root", status: "compatible" },
                { id: "JEI", type: "mod", status: "compatible" },
                { id: "Optifine", type: "mod", status: "compatible" },
                { id: "OldPhysics", type: "mod", status: "incompatible" },
                { id: "Patchouli", type: "mod", status: "compatible" }
            ],
            links: [
                { source: "CoreMod", target: "JEI", rel: "required" },
                { source: "CoreMod", target: "Optifine", rel: "optional" },
                { source: "CoreMod", target: "OldPhysics", rel: "conflict" },
                { source: "CoreMod", target: "Patchouli", rel: "required" }
            ]
        };
    }

    // If TEST_MODE is set (string name or boolean true), inject graphData for the visualizer.
    // Usage:
    // - set window.TEST_MODE = true (uses "small")
    // - or window.TEST_MODE = "medium" / "cycle"
    if (typeof window !== "undefined" && window.TEST_MODE) {
        var mode = (typeof window.TEST_MODE === "string") ? window.TEST_MODE : "small";
        window.graphData = getTestGraphData(mode);
        console.info("[test_graph_data] injected test graphData, mode:", mode);
    }

    // expose helper so tests can call it explicitly
    if (typeof window !== "undefined") {
        window.getTestGraphData = getTestGraphData;
    }
})();
