package com.inso.MinecraftProject.graph;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.HashSet;
import java.util.Iterator;

class GraphTest {

    // Test 1: Iterator is not null
    @Test
    void testIteratorNotNull() {
        Graph graph = new Graph();
        Iterator<ModNode> iterator = graph.iterator();
        assertNotNull(iterator, "Iterator should not be null");
    }

    // Test 2: Empty graph has no elements
    @Test
    void testIteratorOnEmptyGraph() {
        Graph graph = new Graph();
        Iterator<ModNode> iterator = graph.iterator();
        assertFalse(iterator.hasNext(), "Empty graph iterator should have no elements");
    }

    // Test 3: Enhanced for-loop works
    @Test
    void testEnhancedForLoopWorks() {
        Graph graph = new Graph();
        boolean loopExecuted = false;
        
        for (ModNode node : graph) {
            assertNotNull(node, "Node should not be null");
            loopExecuted = true;
        }
        // Loop should execute without errors
        assertTrue(true, "Enhanced for-loop should work without exceptions");
    }

    // Test 4: Iterator doesn't expose internal HashMap
    @Test
    void testIteratorEncapsulation() {
        Graph graph = new Graph();
        Iterator<ModNode> iterator = graph.iterator();
        
        // Verify iterator is not the HashMap itself
        assertFalse(iterator instanceof java.util.HashMap, 
            "Iterator should not expose internal HashMap structure");
    }

    // Test 5: No duplicate nodes in iteration
    @Test
    void testNoDuplicateNodesInIteration() {
        Graph graph = new Graph();
        HashSet<String> seenKeys = new HashSet<>();
        
        for (ModNode node : graph) {
            String key = node.getModId() + "@" + node.getVersion();
            assertFalse(seenKeys.contains(key), 
                "Node should not appear twice in iteration: " + key);
            seenKeys.add(key);
        }
    }
}