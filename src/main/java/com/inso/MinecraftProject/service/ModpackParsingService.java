package com.inso.MinecraftProject.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ModpackParsingService {

    public String parseModpack() {
        return "Parsing Modpack";
    }

    //Function that return a List with 3 Lists inside that have all the data
    public List<List<String>> ReadJson(JarFile jarFile){

        //Creates List for store results
        List<String> Depends = new ArrayList<>();
        List<String> Breaks = new ArrayList<>();
        List<String> Conflicts = new ArrayList<>();
        List<List<String>> Results = new ArrayList<>();

        //Try and Catch to go around Null errors
        try {

            //Get info in jarFile
            Enumeration<JarEntry> entries = jarFile.entries();

            //Jackson parser for reading JSON
            ObjectMapper mapper = new ObjectMapper();

            //Go searching the .Json
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();

                // When .Json is found go into the file
                if (entry.getName().equals("fabric.mod.json")) {

                    InputStream is = jarFile.getInputStream(entry);
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
}