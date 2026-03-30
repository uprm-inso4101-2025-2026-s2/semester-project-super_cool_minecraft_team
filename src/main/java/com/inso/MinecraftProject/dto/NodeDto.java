package com.inso.MinecraftProject.dto;

import lombok.Builder;

/**
 * Data transfer object representing a graph node for frontend visualization.
 */
public class NodeDto {

    private String id;
    private String type;

    public NodeDto() {
    }

    /**
     * Creates a node DTO with all required fields.
     *
     * @param id unique identifier of the node
     * @param type type/category of the node
     */
    @Builder
    public NodeDto(String id, String type) {
        this.id = id;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}