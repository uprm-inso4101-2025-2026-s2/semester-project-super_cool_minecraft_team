package com.inso.MinecraftProject.service;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.zip.*;

import org.springframework.stereotype.Service;

import com.inso.MinecraftProject.dto.DTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ModpackParsingService {

    public DTO parseModpack() {
        return DTO.builder()
                .mods(List.of())
                .edges(List.of())
                .missingDependencies(null)
                .resolvedDependencies(null)
                .build();
    }

    //Function that return a List with 3 Lists inside that have all the data
    public List<List<String>> ReadJson(JarInputStream jarInputStream) {
        List<String> Depends = new ArrayList<>();
        List<String> Breaks = new ArrayList<>();
        List<String> Conflicts = new ArrayList<>();
        List<List<String>> Results = new ArrayList<>();

        //Try and Catch to go around Null errors
        try {

            //Get info in jarFile

            //Jackson parser for reading JSON
            ObjectMapper mapper = new ObjectMapper();

            //Go searching the .Json
            JarEntry entry;
            while ((entry = jarInputStream.getNextJarEntry()) != null) {

                // When .Json is found go into the file
                if (entry.getName().equals("fabric.mod.json")) {

                    InputStream is = jarInputStream;
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                    StringBuilder content = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        content.append(line);
                    }

                    reader.close();

                    try {
                        //Parse JSON using Jackson
                        JsonNode json = mapper.readTree(content.toString());

                        //Extract "breaks" and add them to Breaks List
                        if (json.has("breaks")) {
                            JsonNode breaksNode = json.get("breaks");
                            breaksNode.fieldNames().forEachRemaining(Breaks::add);
                        }

                        //Extract "conflicts" and add them to Conflict List
                        if (json.has("conflicts")) {
                            JsonNode conflictsNode = json.get("conflicts");
                            conflictsNode.fieldNames().forEachRemaining(Conflicts::add);
                        }

                        //Extract "depends" and add them to Depends List
                        if (json.has("depends")) {
                            JsonNode dependsNode = json.get("depends");
                            dependsNode.fieldNames().forEachRemaining(Depends::add);
                        }

                    } catch (Exception e) {
                        // Not all JSON files are mod metadata, ignore this part
                    }

                    //Stop after finding metadata file
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        //Add the Lists to Result
        System.out.println(Depends);
        System.out.println(Conflicts);
        System.out.println(Breaks);

        Results.add(Depends);
        Results.add(Conflicts);
        Results.add(Breaks);

        //Returns the List with all the needed Lists
        return Results;
    }

    public List<List<List<String>>> readZipFile(MultipartFile file) {
        List<List<List<String>>> data = new ArrayList<>();
        try (InputStream is = file.getInputStream(); ZipInputStream zis = new ZipInputStream(is)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().toLowerCase(Locale.ROOT).endsWith(".jar")) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        baos.write(buffer, 0, len);
                    }
                    JarInputStream jis = new JarInputStream(
                            new ByteArrayInputStream(baos.toByteArray())
                    );
                    data.add(ReadJson(jis));
                }
            }
        } catch (IOException e) {
            return data;
        }
        return data;
    }
}
