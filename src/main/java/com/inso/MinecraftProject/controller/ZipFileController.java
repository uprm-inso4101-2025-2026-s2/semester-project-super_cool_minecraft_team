package com.inso.MinecraftProject.controller;

import com.inso.MinecraftProject.dto.DTO;
import com.inso.MinecraftProject.service.ModpackParsingService;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.net.URI;
import java.util.Locale;
import java.util.Map;

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

        DTO graphData = modpackParsingService.parseModpack();
        session.setAttribute("graphDto", graphData);

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, URI.create("/graph").toString())
                .build();
    }
}
