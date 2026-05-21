package com.inso.MinecraftProject.controller;

import com.inso.MinecraftProject.dto.DTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration evidence: multipart upload is accepted, the zip is processed, and the graph DTO is stored in session.
 */
@SpringBootTest
class ZipFileControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void uploadModpackZip_savesDtoInSession_returnsOkJson() throws Exception {
        byte[] zipBytes = minimalFabricModpackZip("integrationtestmod", "1.2.3");
        MockMultipartFile multipart = new MockMultipartFile(
                "file", "pack.zip", "application/zip", zipBytes);
        MockHttpSession session = new MockHttpSession();

        mvc.perform(multipart("/api/modpack/zip")
                        .file(multipart)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.redirect").value("/graph"));

        assertThat(session.getAttribute("graphDto")).isInstanceOf(DTO.class);
        DTO dto = (DTO) session.getAttribute("graphDto");
        assertThat(dto.getMods()).anyMatch(m -> "integrationtestmod".equals(m.getId()));
    }

    @Test
    void uploadEmptyFile_returnsBadRequest() throws Exception {
        MockMultipartFile multipart = new MockMultipartFile(
                "file", "empty.zip", "application/zip", new byte[0]);

        mvc.perform(multipart("/api/modpack/zip").file(multipart))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No file uploaded or file is empty."));
    }

    @Test
    void uploadNonZipFile_returnsBadRequest() throws Exception {
        MockMultipartFile multipart = new MockMultipartFile(
                "file", "mods.txt", "text/plain", "not a zip".getBytes(StandardCharsets.UTF_8));

        mvc.perform(multipart("/api/modpack/zip").file(multipart))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only .zip files are supported."));
    }

    private static byte[] minimalFabricModpackZip(String modId, String version) throws Exception {
        byte[] innerJar = minimalFabricJarZip(modId, version);
        ByteArrayOutputStream outer = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(outer)) {
            ZipEntry modEntry = new ZipEntry("mods/" + modId + ".jar");
            zos.putNextEntry(modEntry);
            zos.write(innerJar);
            zos.closeEntry();
        }
        return outer.toByteArray();
    }

    private static byte[] minimalFabricJarZip(String modId, String version) throws Exception {
        String json = String.format(
                "{\"id\":\"%s\",\"version\":\"%s\",\"depends\":{}}",
                modId,
                version);
        ByteArrayOutputStream jar = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(jar)) {
            ZipEntry meta = new ZipEntry("fabric.mod.json");
            zos.putNextEntry(meta);
            zos.write(json.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }
        return jar.toByteArray();
    }
}
