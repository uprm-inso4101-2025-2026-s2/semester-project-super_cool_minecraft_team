package com.inso.MinecraftProject.graph;

import com.inso.MinecraftProject.dto.DTO;
import com.inso.MinecraftProject.entity.Mod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
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
     @Test
    public void testFindNode() {
        Graph graph = new Graph();

        Set<String> deps = new HashSet<>();
        Set<String> conf = new HashSet<>();

        ModNode node = new ModNode("modA", "1.0", deps, conf);
        Assertions.assertTrue(graph.addNode(node), "addNode should return true for a new node");

        String key = graph.generateKey(node);
        ModNode found = graph.findNode(key);
        Assertions.assertNotNull(found, "findNode should return the previously added node");
        Assertions.assertEquals("modA", found.getModId());
        Assertions.assertEquals("1.0", found.getVersion());

        Assertions.assertFalse(graph.addNode(node), "addNode should return false for duplicate node");

        Assertions.assertNull(graph.findNode(null), "findNode should return null for null key");

        Assertions.assertTrue(graph.removeNode(key), "removeNode should succeed for existing key");
        Assertions.assertNull(graph.findNode(key), "Node should not be found after removal");

        Assertions.assertThrows(IllegalArgumentException.class, () -> graph.generateKey(null));
    }
    

}
