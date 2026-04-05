package com.inso.MinecraftProject.service;

import java.util.*;

import com.inso.MinecraftProject.dto.DTO;
import org.springframework.stereotype.Service;

import com.inso.MinecraftProject.dto.GraphResponseDto;
import com.inso.MinecraftProject.dto.LinkDto;
import com.inso.MinecraftProject.dto.NodeDto;
import com.inso.MinecraftProject.graph.Graph;
import com.inso.MinecraftProject.graph.ModNode;

@Service
public class GraphService implements IGraphService {
    private Graph graph;

    public GraphService() {
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
    public List<LinkDto> mapDependencies(ModNode node) {
        // Map each dependency into a LinkDto where the source is this node and the target is the dependency.
        // Relationship type set to "required".
        if (node == null) {
            return Collections.emptyList();
        }
        Set<String> dependencies = node.getDependencies();
        if (dependencies == null || dependencies.isEmpty()) {
            return Collections.emptyList();
        }

        List<LinkDto> links = new ArrayList<>();
        String source = graph.generateKey(node);
        for (String dependency : dependencies) {
            links.add(LinkDto.builder().source(source).target(dependency).rel("required").build());
        }
        return links;
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

    @Override
    public void setGraphData(DTO dto) {
        graph = new Graph(dto);
    }
}
