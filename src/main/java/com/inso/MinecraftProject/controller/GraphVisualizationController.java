package com.inso.MinecraftProject.controller;

import com.inso.MinecraftProject.dto.DTO;
import com.inso.MinecraftProject.dto.GraphResponseDto;
import com.inso.MinecraftProject.service.GraphService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GraphVisualizationController {

    private final GraphService graphService;

    public GraphVisualizationController(GraphService service) {
        this.graphService = service;
    }

    @GetMapping("/graph")
    public String showGraph(Model model, HttpSession session) {
        DTO dto = session.getAttribute("graphDto") instanceof DTO ? ((DTO) session.getAttribute("graphDto")) : null;

        if (dto != null) {
            graphService.setGraphData(dto);
        }

        GraphResponseDto graphData = graphService.getGraphData();

        session.setAttribute("graphData", graphData);
        model.addAttribute("graphData", graphData);
        return "graph_visualization";
    }

}