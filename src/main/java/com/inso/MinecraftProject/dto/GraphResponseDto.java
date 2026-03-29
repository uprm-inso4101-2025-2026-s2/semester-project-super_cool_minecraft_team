package com.inso.MinecraftProject.dto;

import java.util.List;

/**
 * Data transfer object representing the full graph response for frontend visualization.
 */
public class GraphResponseDto {

    private List<NodeDto> nodes;
    private List<LinkDto> links;

    /**
     * Default constructor.
     */
    public GraphResponseDto() {
    }

    /**
     * Creates a graph response DTO with nodes and links.
     *
     * @param nodes list of graph nodes
     * @param links list of graph links
     */
    public GraphResponseDto(List<NodeDto> nodes, List<LinkDto> links) {
        this.nodes = nodes;
        this.links = links;
    }

    /**
     * Returns the list of graph nodes.
     *
     * @return nodes list
     */
    public List<NodeDto> getNodes() {
        return nodes;
    }

    /**
     * Returns the list of graph links.
     *
     * @return links list
     */
    public List<LinkDto> getLinks() {
        return links;
    }
}