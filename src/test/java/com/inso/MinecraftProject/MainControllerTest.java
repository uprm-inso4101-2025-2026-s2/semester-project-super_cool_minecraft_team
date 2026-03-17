package com.inso.MinecraftProject;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MainController.class)
class MainControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void missingDependenciesPageLoads() throws Exception {
        mvc.perform(get("/missing-dependencies"))
                .andExpect(status().isOk())
                .andExpect(view().name("missing-dependencies"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("How to install")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("What to do if unresolved")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("re-run analysis")));
    }
}
