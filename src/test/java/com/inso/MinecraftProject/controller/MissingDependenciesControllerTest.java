package com.inso.MinecraftProject.controller;

import com.inso.MinecraftProject.dto.DTO;
import com.inso.MinecraftProject.entity.Mod;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MissingDependenciesController.class)
class MissingDependenciesControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void missingDependenciesPageLoads() throws Exception {
        mvc.perform(get("/missing-dependencies"))
                .andExpect(status().isOk())
                .andExpect(view().name("missing_dependencies"))
                .andExpect(model().attribute("loader", "Fabric"));
    }

    @Test
    void apiReturnsEmptyListsAndErrorWhenNoSessionDtoExists() throws Exception {
        mvc.perform(get("/api/missing-dependencies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.missingDependencies", hasSize(0)))
                .andExpect(jsonPath("$.resolvedDependencies", hasSize(0)))
                .andExpect(jsonPath("$.error", is("No analysis result found. Run graph analysis first.")));
    }

    @Test
    void apiReturnsPopulatedDataWhenSessionDtoExists() throws Exception {
        MockHttpSession session = new MockHttpSession();

        Mod missingMod = Mod.builder()
                .id("fabric-api")
                .version("0.92.0+1.20.1")
                .build();

        DTO dto = DTO.builder()
                .missingDependencies(List.of(missingMod))
                .resolvedDependencies(List.of("https://modrinth.com/mod/fabric-api"))
                .build();

        session.setAttribute("graphDto", dto);

        mvc.perform(get("/api/missing-dependencies").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.missingDependencies", hasSize(1)))
                .andExpect(jsonPath("$.missingDependencies[0].id", is("fabric-api")))
                .andExpect(jsonPath("$.resolvedDependencies", hasSize(1)))
                .andExpect(jsonPath("$.resolvedDependencies[0]", is("https://modrinth.com/mod/fabric-api")));
    }

    @Test
    void apiHandlesNullFieldsGracefully() throws Exception {
        MockHttpSession session = new MockHttpSession();

        DTO dto = new DTO();
        dto.setMissingDependencies(null);
        dto.setResolvedDependencies(null);

        session.setAttribute("graphDto", dto);

        mvc.perform(get("/api/missing-dependencies").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.missingDependencies", hasSize(0)))
                .andExpect(jsonPath("$.resolvedDependencies", hasSize(0)));
    }

    @Test
    void apiTreatsInvalidSessionAttributeAsMissingDto() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("graphDto", "not-a-dto");

        mvc.perform(get("/api/missing-dependencies").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.missingDependencies", hasSize(0)))
                .andExpect(jsonPath("$.resolvedDependencies", hasSize(0)))
                .andExpect(jsonPath("$.error", is("No analysis result found. Run graph analysis first.")));
    }
}