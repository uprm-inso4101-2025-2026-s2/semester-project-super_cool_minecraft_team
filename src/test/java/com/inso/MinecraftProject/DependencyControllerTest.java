package com.inso.MinecraftProject;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import com.inso.MinecraftProject.service.DependencyController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DependencyController.class)
public class DependencyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturn200ForDefaultMode() throws Exception {
        // cambio:
        // valida que sin pasar mode, el endpoint siga funcionando
        // o sea, comportamiento por defecto = ok
        mockMvc.perform(get("/api/dependencies/missing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.missingDependencies[0].id").value("fabric-api"))
                .andExpect(jsonPath("$.resolvedDependencies[0]").value("https://modrinth.com/mod/fabric-api"));
    }

    @Test
    void shouldReturn200ForOkMode() throws Exception {
        // cambio:
        // prueba explícita del modo ok
        mockMvc.perform(get("/api/dependencies/missing?mode=ok"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.missingDependencies[0].id").value("fabric-api"))
                .andExpect(jsonPath("$.missingDependencies[0].version").value("0.100.1"))
                .andExpect(jsonPath("$.resolvedDependencies[0]").value("https://modrinth.com/mod/fabric-api"));
    }

    @Test
    void shouldReturn200ForPartialMode() throws Exception {
        // cambio:
        // valida el caso partial:
        // hay más mods faltantes que links resueltos
        mockMvc.perform(get("/api/dependencies/missing?mode=partial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.missingDependencies[0].id").value("fabric-api"))
                .andExpect(jsonPath("$.missingDependencies[1].id").value("cloth-config"))
                .andExpect(jsonPath("$.resolvedDependencies[0]").value("https://modrinth.com/mod/fabric-api"));
    }

    @Test
    void shouldReturn429ForFail429Mode() throws Exception {
        // cambio:
        // valida simulación de rate limit
        mockMvc.perform(get("/api/dependencies/missing?mode=fail429"))
                .andExpect(status().isTooManyRequests())
                .andExpect(content().string("Rate limit simulated"));
    }

    @Test
    void shouldReturn500ForFail500Mode() throws Exception {
        // cambio:
        // valida simulación de server error
        mockMvc.perform(get("/api/dependencies/missing?mode=fail500"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Server error simulated"));
    }

    @Test
    void shouldReturn200ForTimeoutModeAfterDelay() throws Exception {
        // cambio:
        // valida que el modo timeout realmente espera antes de responder
        long start = System.currentTimeMillis();

        mockMvc.perform(get("/api/dependencies/missing?mode=timeout"))
                .andExpect(status().isOk());

        long end = System.currentTimeMillis();
        long elapsed = end - start;

        assertTrue(elapsed >= 9000, "Timeout mode should delay the response");
    }
}
