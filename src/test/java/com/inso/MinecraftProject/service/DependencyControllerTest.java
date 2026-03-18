package com.inso.MinecraftProject.service;

import com.inso.MinecraftProject.dto.ResolvedDependencyDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DependencyController.class)
class DependencyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DependencyLookupService dependencyLookupService;

    @Test
    void shouldReturn200ForDefaultMode() throws Exception {
        when(dependencyLookupService.resolveDependencies(anyList())).thenReturn(List.of(
                new ResolvedDependencyDto(
                        "fabric-api",
                        "Fabric API",
                        List.of("https://modrinth.com/mod/fabric-api"),
                        "https://modrinth.com/mod/fabric-api"
                ),
                new ResolvedDependencyDto(
                        "modmenu",
                        "Mod Menu",
                        List.of("https://modrinth.com/mod/modmenu"),
                        "https://modrinth.com/mod/modmenu"
                )
        ));

        mockMvc.perform(get("/api/dependencies/missing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.missingDependencies[0].id").value("fabric-api"))
                .andExpect(jsonPath("$.missingDependencies[1].id").value("modmenu"))
                .andExpect(jsonPath("$.resolvedDependencies[0].id").value("fabric-api"))
                .andExpect(jsonPath("$.resolvedDependencies[0].preferred").value("https://modrinth.com/mod/fabric-api"))
                .andExpect(jsonPath("$.analysisHasPartialResults").value(false));
    }

    @Test
    void shouldReturn200ForOkMode() throws Exception {
        when(dependencyLookupService.resolveDependencies(anyList())).thenReturn(List.of(
                new ResolvedDependencyDto(
                        "fabric-api",
                        "Fabric API",
                        List.of("https://modrinth.com/mod/fabric-api"),
                        "https://modrinth.com/mod/fabric-api"
                )
        ));

        mockMvc.perform(get("/api/dependencies/missing?mode=ok"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.missingDependencies[0].id").value("fabric-api"))
                .andExpect(jsonPath("$.missingDependencies[0].requiredVersion").value("0.100.1"))
                .andExpect(jsonPath("$.resolvedDependencies[0].links[0]").value("https://modrinth.com/mod/fabric-api"))
                .andExpect(jsonPath("$.analysisHasPartialResults").value(true));
    }

    @Test
    void shouldReturn200ForPartialMode() throws Exception {
        when(dependencyLookupService.resolveDependencies(anyList())).thenReturn(List.of(
                new ResolvedDependencyDto(
                        "fabric-api",
                        "Fabric API",
                        List.of("https://modrinth.com/mod/fabric-api"),
                        "https://modrinth.com/mod/fabric-api"
                ),
                new ResolvedDependencyDto(
                        "cloth-config",
                        "Cloth Config",
                        List.of("https://modrinth.com/mod/cloth-config"),
                        "https://modrinth.com/mod/cloth-config"
                )
        ));

        mockMvc.perform(get("/api/dependencies/missing?mode=partial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.missingDependencies[0].id").value("fabric-api"))
                .andExpect(jsonPath("$.missingDependencies[1].id").value("cloth-config"))
                .andExpect(jsonPath("$.resolvedDependencies.length()").value(1))
                .andExpect(jsonPath("$.resolvedDependencies[0].id").value("fabric-api"))
                .andExpect(jsonPath("$.analysisHasPartialResults").value(true));
    }

    @Test
    void shouldReturnEmptyResponse() throws Exception {
        when(dependencyLookupService.resolveDependencies(anyList())).thenReturn(List.of());

        mockMvc.perform(get("/api/dependencies/missing?mode=empty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.missingDependencies.length()").value(0))
                .andExpect(jsonPath("$.resolvedDependencies.length()").value(0))
                .andExpect(jsonPath("$.analysisHasPartialResults").value(false));
    }

    @Test
    void shouldReturn429ForFail429Mode() throws Exception {
        mockMvc.perform(get("/api/dependencies/missing?mode=fail429"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value("Dependency lookup was rate limited."));

        verifyNoInteractions(dependencyLookupService);
    }

    @Test
    void shouldReturn500ForFail500Mode() throws Exception {
        mockMvc.perform(get("/api/dependencies/missing?mode=fail500"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Dependency lookup failed on the server."));

        verifyNoInteractions(dependencyLookupService);
    }

    @Test
    void shouldReturn504ForTimeoutMode() throws Exception {
        mockMvc.perform(get("/api/dependencies/missing?mode=timeout"))
                .andExpect(status().isGatewayTimeout())
                .andExpect(jsonPath("$.message").value("Dependency lookup timed out."));
    }

    @Test
    void shouldCallLookupServiceForNonErrorModes() throws Exception {
        when(dependencyLookupService.resolveDependencies(anyList())).thenReturn(List.of(
                new ResolvedDependencyDto(
                        "fabric-api",
                        "Fabric API",
                        List.of("https://modrinth.com/mod/fabric-api"),
                        "https://modrinth.com/mod/fabric-api"
                )
        ));

        mockMvc.perform(get("/api/dependencies/missing?mode=ok"))
                .andExpect(status().isOk());

        verify(dependencyLookupService).resolveDependencies(anyList());
    }
}
