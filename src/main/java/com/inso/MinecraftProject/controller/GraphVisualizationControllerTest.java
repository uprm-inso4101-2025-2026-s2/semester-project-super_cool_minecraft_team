package com.inso.MinecraftProject;

import com.inso.MinecraftProject.controller.GraphVisualizationController;
import com.inso.MinecraftProject.dto.DTO;
import com.inso.MinecraftProject.dto.GraphResponseDto;
import com.inso.MinecraftProject.service.GraphService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GraphVisualizationController.class)
class GraphVisualizationControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private GraphService graphService;

    @Test
    void rendersGraphVisualizationWithoutSessionDto() throws Exception {
        GraphResponseDto graphData = GraphResponseDto.builder()
                .nodes(List.of())
                .links(List.of())
                .build();

        when(graphService.getGraphData()).thenReturn(graphData);

        mvc.perform(get("/graph"))
                .andExpect(status().isOk())
                .andExpect(view().name("graph_visualization"))
                .andExpect(model().attribute("hasMissing", false))
                .andExpect(model().attribute("graphData", graphData))
                .andExpect(request().sessionAttribute("graphData", graphData));

        verify(graphService, never()).setGraphData(any());
        verify(graphService, times(1)).getGraphData();
    }

    @Test
    void rendersGraphVisualizationWithSessionDto() throws Exception {
        DTO dto = new DTO();
        GraphResponseDto graphData = GraphResponseDto.builder()
                .nodes(List.of())
                .links(List.of())
                .build();

        when(graphService.getGraphData()).thenReturn(graphData);

        mvc.perform(get("/graph").sessionAttr("graphDto", dto))
                .andExpect(status().isOk())
                .andExpect(view().name("graph_visualization"))
                .andExpect(model().attribute("hasMissing", false))
                .andExpect(model().attribute("graphData", graphData))
                .andExpect(request().sessionAttribute("graphData", graphData));

        verify(graphService, times(1)).setGraphData(dto);
        verify(graphService, times(1)).getGraphData();
    }
}