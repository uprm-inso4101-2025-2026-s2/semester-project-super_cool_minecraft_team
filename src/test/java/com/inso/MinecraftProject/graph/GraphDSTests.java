package com.inso.MinecraftProject.graph;

import com.inso.MinecraftProject.dto.DTO;
import com.inso.MinecraftProject.entity.Mod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Set;

@SpringBootTest
public class GraphDSTests {

    @Test
    public void testConstructor() {
        Mod modA = Mod.builder()
                .id("modA")
                .version("1.0")
                .depends(List.of())
                .breaks(List.of())
                .suggests(List.of())
                .recommends(List.of())
                .conflicts(List.of())
                .build();

        Mod modB = Mod.builder()
                .id("modB")
                .version("1.0")
                .depends(List.of(modA))
                .breaks(List.of())
                .suggests(List.of())
                .recommends(List.of())
                .conflicts(List.of())
                .build();

        Mod modC = Mod.builder()
                .id("modC")
                .version("1.0")
                .depends(List.of(modA))
                .breaks(List.of(modB))
                .suggests(List.of())
                .recommends(List.of())
                .conflicts(List.of())
                .build();

        DTO build = DTO.builder()
                .mods(List.of(modA, modB))
                .missingDependencies(List.of(modC))
                .build();

        Graph graph = new Graph(build);

        ModNode node = graph.findNode("modA@1.0");
        Assertions.assertNotNull(node, "Node for modA should not be null");
        Assertions.assertEquals("modA@1.0", node.getModId(), "Node ID should be 'modA@1.0'");
        Assertions.assertEquals("1.0", node.getVersion(), "Node version should be '1.0'");
        Assertions.assertTrue(node.getDependencies().isEmpty(), "Node dependencies should be empty");
        Assertions.assertTrue(node.getConflicts().isEmpty(), "Node conflicts should be empty");

        node = graph.findNode("modB@1.0");
        Assertions.assertNotNull(node, "Node for modB should not be null");
        Assertions.assertEquals("modB@1.0", node.getModId(), "Node ID should be 'modB@1.0'");
        Assertions.assertEquals("1.0", node.getVersion(), "Node version should be '1.0'");
        Assertions.assertTrue(node.getDependencies().contains("modA@1.0"), "Node dependencies should contain 'modA@1.0'");
        Assertions.assertTrue(node.getConflicts().isEmpty(), "Node conflicts should be empty");

        node = graph.findNode("modC@1.0");
        Assertions.assertNull(node, "Node for modC should be null since it's a missing dependency");
    }

    @Test
    public void testNodeManagement() {
        Graph graph = new Graph();
        ModNode modNode = new ModNode("modD", "1.0", Set.of("modA@1.0"), Set.of("modB@1.0"));
        boolean added = graph.addNode(modNode);
        Assertions.assertTrue(added, "Node should be added successfully");
        ModNode node = graph.findNode("modD@1.0");
        Assertions.assertNotNull(node, "Node for modD should not be null");
        Assertions.assertEquals("modD@1.0", node.getModId(), "Node ID should be 'modD@1.0'");
        Assertions.assertEquals("1.0", node.getVersion(), "Node version should be '1.0'");
        Assertions.assertTrue(node.getDependencies().contains("modA@1.0"), "Node dependencies should contain 'modA@1.0'");
        Assertions.assertTrue(node.getConflicts().contains("modB@1.0"), "Node conflicts should contain 'modB@1.0'");

        added = graph.addNode(modNode);
        Assertions.assertFalse(added, "Adding the same node again should fail");

        boolean removed = graph.removeNode("modD@1.0");
        Assertions.assertTrue(removed, "Node should be removed successfully");
        node = graph.findNode("modD@1.0");
        Assertions.assertNull(node, "Node for modD should be null after removal");

        removed = graph.removeNode("modD@1.0");
        Assertions.assertFalse(removed, "Removing a non-existent node should fail");
    }

}
