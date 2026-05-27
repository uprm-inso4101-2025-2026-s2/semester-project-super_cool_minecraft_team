package com.inso.MinecraftProject.graph;

import com.inso.MinecraftProject.entity.Mod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.Set;

@SpringBootTest
public class GraphDSTests {

    @Test
    public void testConstructor() {

        Graph graph = new Graph();

        Assertions.assertNotNull(
                graph,
                "Graph should not be null"
        );

        Assertions.assertFalse(
                graph.hasModNode("modA@1.0"),
                "Graph should initially be empty"
        );
    }

    @Test
    public void testNodeManagement() {

        Graph graph = new Graph();

        ModNode modNode = new ModNode(
                "modD",
                "1.0",
                new HashSet<>(Set.of("modA@1.0")),
                new HashSet<>(Set.of("modB@1.0"))
        );

        boolean added = graph.addNode(modNode);

        Assertions.assertTrue(
                added,
                "Node should be added successfully"
        );

        String key = graph.generateKey(modNode);

        ModNode node = graph.findNode(key);

        Assertions.assertNotNull(
                node,
                "Node for modD should not be null"
        );

        Assertions.assertEquals(
                "modD",
                node.getModId(),
                "Node ID should be 'modD'"
        );

        Assertions.assertEquals(
                "1.0",
                node.getVersion(),
                "Node version should be '1.0'"
        );

        Assertions.assertTrue(
                node.getDependencies().contains("modA@1.0"),
                "Node dependencies should contain 'modA@1.0'"
        );

        Assertions.assertTrue(
                node.getConflicts().contains("modB@1.0"),
                "Node conflicts should contain 'modB@1.0'"
        );

        added = graph.addNode(modNode);

        Assertions.assertFalse(
                added,
                "Adding the same node again should fail"
        );

        boolean removed = graph.removeNode(key);

        Assertions.assertTrue(
                removed,
                "Node should be removed successfully"
        );

        node = graph.findNode(key);

        Assertions.assertNull(
                node,
                "Node for modD should be null after removal"
        );

        removed = graph.removeNode(key);

        Assertions.assertFalse(
                removed,
                "Removing a non-existent node should fail"
        );
    }

    @Test
    public void testFindNode() {

        Graph graph = new Graph();

        Set<String> deps = new HashSet<>();
        Set<String> conf = new HashSet<>();

        ModNode node = new ModNode(
                "modA",
                "1.0",
                deps,
                conf
        );

        Assertions.assertTrue(
                graph.addNode(node),
                "addNode should return true for a new node"
        );

        String key = graph.generateKey(node);

        ModNode found = graph.findNode(key);

        Assertions.assertNotNull(
                found,
                "findNode should return the previously added node"
        );

        Assertions.assertEquals(
                "modA",
                found.getModId()
        );

        Assertions.assertEquals(
                "1.0",
                found.getVersion()
        );

        Assertions.assertFalse(
                graph.addNode(node),
                "addNode should return false for duplicate node"
        );

        Assertions.assertNull(
                graph.findNode(null),
                "findNode should return null for null key"
        );

        Assertions.assertTrue(
                graph.removeNode(key),
                "removeNode should succeed for existing key"
        );

        Assertions.assertNull(
                graph.findNode(key),
                "Node should not be found after removal"
        );

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> graph.generateKey(null)
        );
    }
}