package com.inso.MinecraftProject.service;

import java.util.zip.ZipFile;

import org.springframework.stereotype.Service;

import com.inso.MinecraftProject.dto.DTO;

@Service
public class ModpackParsingService {

    /**
     * MAIN FUNCTION:
     * Parses a single mod jar pack and returns DTO graph.
     */
    public DTO parseModpack(ZipFile zipFile) {

        ZipProcessingService processing = new ZipProcessingService();

        DTO posibleDto = processing.processModpackZip(zipFile);

        return posibleDto;

    }
}