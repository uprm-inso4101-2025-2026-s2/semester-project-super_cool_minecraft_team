package com.inso.MinecraftProject.dto;

/**
 * Data transfer object representing a relationship between two graph nodes.
 */
public class LinkDto {

    private String source;
    private String target;
    private String rel;

    /**
     * Default constructor.
     */
    public LinkDto() {
    }

    /**
     * Creates a link DTO with all required fields.
     *
     * @param source source node id
     * @param target target node id
     * @param rel relationship type
     */
    public LinkDto(String source, String target, String rel) {
        this.source = source;
        this.target = target;
        this.rel = rel;
    }

    /**
     * Returns the source node id.
     *
     * @return source node id
     */
    public String getSource() {
        return source;
    }

    /**
     * Returns the target node id.
     *
     * @return target node id
     */
    public String getTarget() {
        return target;
    }

    /**
     * Returns the relationship type.
     *
     * @return relationship type
     */
    public String getRel() {
        return rel;
    }
}