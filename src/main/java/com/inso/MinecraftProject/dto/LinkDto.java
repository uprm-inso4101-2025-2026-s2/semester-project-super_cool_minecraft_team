package com.inso.MinecraftProject.dto;

import lombok.Builder;

/**
 * Data transfer object representing a relationship between two graph nodes.
 */
public class LinkDto {

    private String source;
    private String target;
    private String rel;

    public LinkDto() {
    }

    /**
     * Creates a link DTO with all required fields.
     *
     * @param source source node id
     * @param target target node id
     * @param rel relationship type
     */
    @Builder
    public LinkDto(String source, String target, String rel) {
        this.source = source;
        this.target = target;
        this.rel = rel;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public String getRel() {
        return rel;
    }
}