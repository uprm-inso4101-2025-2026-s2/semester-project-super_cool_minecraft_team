package com.inso.MinecraftProject.controller;

import com.inso.MinecraftProject.service.ModpackParsingService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
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
    public ResponseEntity<Map<String, Object>> uploadZipFile(@RequestPart("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "No file uploaded or file is empty."));
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase(Locale.ROOT).endsWith(".zip")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Only .zip files are supported."));
        }

        String parsingStatus = modpackParsingService.parseModpack();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Zip file accepted.");
        response.put("fileName", originalFilename);
        response.put("status", parsingStatus);
        return ResponseEntity.ok(response);
    }
}
