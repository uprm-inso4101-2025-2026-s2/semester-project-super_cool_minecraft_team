package com.inso.MinecraftProject.service; 

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
 
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class ModrinthServiceWrapper{
    public static final String Base_URL = "https://api.modrinth.com/v2";
    private static final String agent = "com.inso.MinecraftProject/1.0";
    private static final int timeout = 10;


    private final HttpClient client;
    private final ObjectMapper mapper;

    public ModrinthServiceWrapper(){
        this.client = HttpClient.newBuilder().
        connectTimeout(Duration.ofSeconds(timeout)).build();
        this.mapper = new ObjectMapper();
    }
            private JsonNode get(String endpoint){
            String url = Base_URL + endpoint;
            HttpRequest request = HttpRequest.newBuilder() //builds the request call to the modrinth api.
            .uri(URI.create(url)).header("User-Agent", agent).build();

            HttpResponse<String> response;
            try{
                response = client.send(request,HttpResponse.BodyHandlers.ofString());
            } catch (Exception e){
                throw new RuntimeException("Failed to send request to Modrinth API" + e.getMessage(), e);
            }
            switch(response.statusCode()){
                case 200 -> {} // sucess; 
                case 401 -> throw new RuntimeException("Unauthorized access to Modrinth API at " + url);
                case 404 -> throw new RuntimeException("Resource not found at endpoint: " + url);
                case 500 -> throw new RuntimeException("Server Issues with Modrinth API at " + url);
                case 503 -> throw new RuntimeException("Service Unavailable at Modrinth API at " + url);
                default -> throw new RuntimeException("Unexpected response code " + response.statusCode() + " from Modrinth API at " + url);

            }
            try{
                return mapper.readTree(response.body());
            } catch (Exception e){
                throw new RuntimeException("Failed to parse JSON response from Modrinth API at " + url + ": " + e.getMessage(), e);
            }
        }

        //methods for calling data 
        //search for a project using a base name and looks through until it reaches its determined limit for the search.
        //Returns a JsonNode containing all the projectID/Slugs or the projects that matched the search.
        public JsonNode searchProject(String name, int limit){
            return get("/search?query="  + URLEncoder.encode(name, StandardCharsets.UTF_8) + "&limit=" + limit);
        }
        //default overide
        public JsonNode searchProject(String name){
            return searchProject(name, 10);
        }
        // Returns full details of the project as a JsonNode for a given project given its ID or slug.
        public JsonNode getProjectById(String slug) throws RuntimeException{
            return get("/project/" + URLEncoder.encode(slug, StandardCharsets.UTF_8));
        }
        //given a slug returns the versions of thep project as a JsonNode.
        public JsonNode getProjectVersions(String slug) throws RuntimeException{
            return get("/project/" + URLEncoder.encode(slug, StandardCharsets.UTF_8) + "/version");
        }
        //Given a version Id returns the specific version details as a JsonNode.
        public JsonNode getVersionById(String versionId) throws RuntimeException{
            return get("/version/" + versionId);
        }
        // Returns all dependencies for a project's versions given its slug.
        // Each dependency includes project_id, version_id, and dependency_type (required, optional, incompatible, embedded).
        public JsonNode getProjectDependencies(String slug) {
        JsonNode versions = getProjectVersions(slug);
        ObjectMapper mapper = new ObjectMapper();
        var dependencies = mapper.createArrayNode();

        for (JsonNode version : versions) {
        JsonNode deps = version.get("dependencies");
        if (deps != null && deps.isArray()) {
            for (JsonNode dep : deps) {
                dependencies.add(dep);
            }
        }
        return dependencies;
    }
}
 }