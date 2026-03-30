package com.inso.MinecraftProject;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RedirectController.class)
class RedirectControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void redirectsAllowedDomain() throws Exception {
        mvc.perform(get("/r").param("u", "https://modrinth.com/mod/sodium"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://modrinth.com/mod/sodium"));
    }

    @Test
    void blocksDisallowedDomain() throws Exception {
        mvc.perform(get("/r").param("u", "https://evil.com/phish"))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectsMalformedUrl() throws Exception {
        mvc.perform(get("/r").param("u", "http://"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsEmptyParam() throws Exception {
        mvc.perform(get("/r"))
                .andExpect(status().isBadRequest());
    }
}
