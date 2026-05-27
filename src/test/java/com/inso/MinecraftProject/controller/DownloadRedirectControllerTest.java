package com.inso.MinecraftProject.controller;

import com.inso.MinecraftProject.dto.ResolvedDependencyDto;
import com.inso.MinecraftProject.service.DependencyLookupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DownloadRedirectController.class)
class DownloadRedirectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DependencyLookupService dependencyLookupService;

    @Test
    void redirectsWhenPreferredLinkExists() throws Exception {
        when(dependencyLookupService.resolveDependencyById("sodium")).thenReturn(
                Optional.of(new ResolvedDependencyDto(
                        "sodium",
                        "Sodium",
                        List.of("https://modrinth.com/mod/sodium"),
                        "https://modrinth.com/mod/sodium"
                ))
        );

        mockMvc.perform(get("/dependencies/sodium/download"))
                .andExpect(status().isFound())
                .andExpect(header().string(LOCATION, "https://modrinth.com/mod/sodium"));
    }

    @Test
    void returnsNotFoundWhenDependencyCannotBeResolved() throws Exception {
        when(dependencyLookupService.resolveDependencyById("missing-mod"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/dependencies/missing-mod/download"))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsNotFoundWhenPreferredLinkIsNull() throws Exception {
        when(dependencyLookupService.resolveDependencyById("no-link")).thenReturn(
                Optional.of(new ResolvedDependencyDto(
                        "no-link",
                        "No Link Mod",
                        List.of(),
                        null
                ))
        );

        mockMvc.perform(get("/dependencies/no-link/download"))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsNotFoundWhenPreferredLinkIsBlank() throws Exception {
        when(dependencyLookupService.resolveDependencyById("blank-link")).thenReturn(
                Optional.of(new ResolvedDependencyDto(
                        "blank-link",
                        "Blank Link Mod",
                        List.of(),
                        "   "
                ))
        );

        mockMvc.perform(get("/dependencies/blank-link/download"))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsBadRequestWhenServiceThrowsIllegalArgumentException() throws Exception {
        when(dependencyLookupService.resolveDependencyById("bad-id"))
                .thenThrow(new IllegalArgumentException("Project id cannot be blank."));

        mockMvc.perform(get("/dependencies/bad-id/download"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsInternalServerErrorWhenServiceThrowsUnexpectedException() throws Exception {
        when(dependencyLookupService.resolveDependencyById("explode"))
                .thenThrow(new RuntimeException("Unexpected failure"));

        mockMvc.perform(get("/dependencies/explode/download"))
                .andExpect(status().isInternalServerError());
    }
}