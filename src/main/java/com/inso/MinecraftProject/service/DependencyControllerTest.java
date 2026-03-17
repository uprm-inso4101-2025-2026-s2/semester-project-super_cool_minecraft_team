package com.inso.MinecraftProject.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DependencyController.class)
public class DependencyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/dependencies/missing"))
                .andExpect(status().isOk());
    }
}