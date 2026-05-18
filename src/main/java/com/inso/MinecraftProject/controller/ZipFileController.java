package com.inso.MinecraftProject.controller;

import com.inso.MinecraftProject.dto.DTO;
import com.inso.MinecraftProject.service.ModpackParsingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipFile;

@RestController
@RequestMapping("/api/modpack")
public class ZipFileController {

    private final ModpackParsingService modpackParsingService;

    public ZipFileController(ModpackParsingService modpackParsingService) {
        this.modpackParsingService = modpackParsingService;
    }

    @PostMapping(
            path = "/zip",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> uploadZipFile(@RequestPart("file") MultipartFile file, HttpSession session) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "No file uploaded or file is empty."));
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase(Locale.ROOT).endsWith(".zip")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Only .zip files are supported."));
        }

        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("modpack-upload-", ".zip");
            file.transferTo(tempFile);

            try (ZipFile zipFile = new ZipFile(tempFile.toFile())) {
                DTO graphData = modpackParsingService.parseModpack(zipFile);
                session.setAttribute("graphDto", graphData);
            }

            return ResponseEntity.ok(Map.of(
                    "ok", true,
                    "redirect", "/graph"
            ));
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to process modpack: " + ex.getMessage()));
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                }
            }
        }
    }
}
