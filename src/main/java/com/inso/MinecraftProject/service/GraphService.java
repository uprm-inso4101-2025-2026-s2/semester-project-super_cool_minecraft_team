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
    public GraphResponseDto getGraphData() {
        // Initialize empty lists for nodes and links
        List<NodeDto> nodes = new ArrayList<>();
        List<LinkDto> links = new ArrayList<>();
        
        // Iterate through all ModNode objects in the graph using the iterator
        for (ModNode node : graph) {
            // Convert each ModNode to a NodeDto and add to nodes list
            NodeDto nodeDto = mapToNodeDTO(node);
            if (nodeDto != null) {
                nodes.add(nodeDto);
            }
            
            // Map all dependencies for this node and add to links list
            List<LinkDto> dependencyLinks = mapDependencies(node);
            links.addAll(dependencyLinks);
            
            // Map all conflicts for this node and add to links list
            List<LinkDto> conflictLinks = mapConflicts(node);
            links.addAll(conflictLinks);
        }
        
        // Combine all nodes and links into a GraphResponseDto and return
        return GraphResponseDto.builder()
                .nodes(nodes)
                .links(links)
                .build();
    }
}
