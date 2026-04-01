package com.inso.MinecraftProject.service;

import java.util.*;

import org.springframework.stereotype.Service;

import com.inso.MinecraftProject.dto.GraphResponseDTO;
import com.inso.MinecraftProject.dto.LinkDTO;
import com.inso.MinecraftProject.dto.NodeDTO;
import com.inso.MinecraftProject.graph.Graph;
import com.inso.MinecraftProject.graph.ModNode;

@Service
public class GraphService {
    private final Graph graph;

    public GraphService(Graph graph){
        this.graph = graph;
    }

    /**
     * Converts the internal graph into a DTO for frontend use.
     *
     * @return GraphResponseDTO containing nodes and links
     */

    public GraphResponseDTO getGraphResponse(){
        List<NodeDTO> nodes = new ArrayList<>();
        List<LinkDTO> links = new ArrayList<>();

        for (ModNode node: graph){
            String nodeKey;
            try {
                nodeKey = graph.generateKey(node);
            } catch (IllegalArgumentException e ){
                continue;
            }

            nodes.add(NodeDTO.builder().id(nodeKey).type("mod").build());

            Set<String> dependencies = node.getDependencies();
            if (dependencies != null) {
                for (String dep: dependencies){
                    links.add(LinkDTO.builder().source(nodeKey).target(dep).rel("dependency").build());
                }
            }

            Set<String> conflicts = node.getConflicts();
            if (conflicts != null){
                for (String conflict: conflicts){
                    links.add(LinkDTO.builder().source(nodeKey).target(conflict).rel("conflict").build());
                }
            }
        }

        return new GraphResponseDTO(nodes,links);
    }
    
    /**
     * Provides controlled access to the graph instance.
     */
    public Graph getGraph(){
        return graph;
    }

}
