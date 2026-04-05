package com.inso.MinecraftProject.service;

import java.util.List;

import com.inso.MinecraftProject.dto.DTO;
import com.inso.MinecraftProject.dto.GraphResponseDto;
import com.inso.MinecraftProject.dto.NodeDto;
import com.inso.MinecraftProject.dto.LinkDto;
import com.inso.MinecraftProject.graph.ModNode;

/**
 * Interface for GraphService class
 */
public interface IGraphService {

    /**
     * Builds and returns the full graph response DTO used by the frontend
     * visualization.
     *
     * @return a {@link GraphResponseDto} containing nodes and links
     */
    GraphResponseDto getGraphData();

    /**
     * Maps an internal {@link ModNode} to a frontend {@link NodeDto}.
     *
     * @param node the internal mod node to map
     * @return a {@link NodeDto} representing the node for the frontend, or null if the input is null
     */
    NodeDto mapToNodeDTO(ModNode node);

    /**
     * Maps the dependency relationships of the provided {@link ModNode}
     * into a list of {@link LinkDto} objects.
     *
     * @param node the node whose dependencies should be mapped
     * @return a list of {@link LinkDto} describing dependency links (empty list if none or input is null)
     */
    List<LinkDto> mapDependencies(ModNode node);

    /**
     * Maps the conflict relationships of the provided {@link ModNode}
     * into a list of {@link LinkDto} objects.
     *
     * @param node the node whose conflicts should be mapped
     * @return a list of {@link LinkDto} describing conflict links (empty list if none or input is null)
     */
    List<LinkDto> mapConflicts(ModNode node);


    /**
     * Sets the graph data based on the provided DTO. This method is responsible for
     * initializing the internal graph structure using the data contained in the DTO.
     *
     * @param dto the data transfer object containing the information needed to build the graph
     */
    public void setGraphData(DTO dto);
}
