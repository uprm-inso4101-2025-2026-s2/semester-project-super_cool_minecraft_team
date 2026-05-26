package com.inso.MinecraftProject.dto;

import java.util.List;
import lombok.Builder;

/**
 * Data transfer object representing the full graph response for frontend visualization.
 */
public class GraphResponseDto {

    private List<NodeDto> nodes;
    private List<LinkDto> links;

    public GraphResponseDto() {
    }

    /**
     * Creates a graph response DTO with nodes and links.
     *
     * @param nodes list of graph nodes
     * @param links list of graph links
     */
    @Builder
    public GraphResponseDto(List<NodeDto> nodes, List<LinkDto> links) {
        this.nodes = nodes;
        this.links = links;
    }

    public List<NodeDto> getNodes() {
        return nodes;
    }

    public List<LinkDto> getLinks() {
        return links;
    }
}