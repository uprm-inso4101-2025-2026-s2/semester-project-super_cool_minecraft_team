package com.inso.MinecraftProject.dto;

/**
 * Data transfer object representing a graph node for frontend visualization.
 */
public class NodeDto {

    private String id;
    private String type;

    /**
     * Default constructor.
     */
    public NodeDto() {
    }

    /**
     * Creates a node DTO with all required fields.
     *
     * @param id unique identifier of the node
     * @param type type/category of the node
     */
    public NodeDto(String id, String type) {
        this.id = id;
        this.type = type;
    }

    /**
     * Returns the node identifier.
     *
     * @return node id
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the node type.
     *
     * @return node type
     */
    public String getType() {
        return type;
    }
}