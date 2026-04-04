package com.inso.MinecraftProject.service;

import java.util.*;

import org.springframework.stereotype.Service;

import com.inso.MinecraftProject.dto.GraphResponseDTO;
import com.inso.MinecraftProject.dto.LinkDTO;
import com.inso.MinecraftProject.dto.NodeDTO;
import com.inso.MinecraftProject.graph.Graph;
import com.inso.MinecraftProject.graph.ModNode;

@Service
public class GraphService implements IGraphService {
    private final Graph graph;

    public GraphService(Graph graph){
        this.graph = graph;
    }
}
