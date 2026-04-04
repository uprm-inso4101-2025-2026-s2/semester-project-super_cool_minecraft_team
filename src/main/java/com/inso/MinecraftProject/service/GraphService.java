package com.inso.MinecraftProject.service;

import java.util.*;

import org.springframework.stereotype.Service;

import com.inso.MinecraftProject.dto.GraphResponseDto;
import com.inso.MinecraftProject.dto.LinkDto;
import com.inso.MinecraftProject.dto.NodeDto;
import com.inso.MinecraftProject.graph.Graph;
import com.inso.MinecraftProject.graph.ModNode;

@Service
public class GraphService implements IGraphService {
    private final Graph graph;

    public GraphService(Graph graph) {
        this.graph = graph;
    }

    @Override
    public NodeDto mapToNodeDTO(ModNode node) {
        if (node == null) {
            return null;
        }
        try {
            // Create and return NodeDto instance with id generated from the graph and type set to "mod"
            String id = graph.generateKey(node);
            String type = "mod";
            return NodeDto.builder().id(id).type(type).build();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public List<LinkDto> mapConflicts(ModNode node) {
        // Map each conflict into a LinkDto where the source is this node
        // and the target is the conflicting node. Relationship type set to "conflicts".
        if (node == null) {
            return Collections.emptyList();
        }
        Set<String> conflicts = node.getConflicts();
        if (conflicts == null || conflicts.isEmpty()) {
            return Collections.emptyList();
        }

        List<LinkDto> links = new ArrayList<>();
        String source = graph.generateKey(node);
        for (String conflict : conflicts) {
            // source (this node) -> target (conflicting node)
            links.add(LinkDto.builder().source(source).target(conflict).rel("conflicts").build());
        }
        return links;
    }
}
