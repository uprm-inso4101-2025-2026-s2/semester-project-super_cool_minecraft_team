package com.inso.MinecraftProject.service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.inso.MinecraftProject.dto.DTO;
import com.inso.MinecraftProject.dto.GraphResponseDto;
import com.inso.MinecraftProject.dto.LinkDto;
import com.inso.MinecraftProject.dto.NodeDto;
import com.inso.MinecraftProject.entity.Mod;
import com.inso.MinecraftProject.graph.ModNode;

class GraphServiceTest {

    private GraphService graphService;

    @BeforeEach
    void setUp() {
        graphService = new GraphService();
    }

    @Test
    void mapToNodeDTO_returnsNull_whenNodeIsNull() {
        assertNull(graphService.mapToNodeDTO(null));
    }

    @Test
    void mapToNodeDTO_returnsNodeDto_whenNodeIsValid() {
        graphService.setGraphData(new DTO());

        ModNode node = new ModNode(
                "jei",
                "1.0.0",
                Collections.emptySet(),
                Collections.emptySet()
        );

        NodeDto result = graphService.mapToNodeDTO(node);

        assertNotNull(result);
        assertEquals("jei@1.0.0", result.getId());
        assertEquals("mod", result.getType());
    }

    @Test
    void mapToNodeDTO_returnsNull_whenNodeHasNullId() {
        graphService.setGraphData(new DTO());

        ModNode node = new ModNode(
                null,
                "1.0.0",
                Collections.emptySet(),
                Collections.emptySet()
        );

        assertNull(graphService.mapToNodeDTO(node));
    }

    @Test
    void mapToNodeDTO_returnsNull_whenNodeHasNullVersion() {
        graphService.setGraphData(new DTO());

        ModNode node = new ModNode(
                "jei",
                null,
                Collections.emptySet(),
                Collections.emptySet()
        );

        assertNull(graphService.mapToNodeDTO(node));
    }

    @Test
    void mapDependencies_returnsEmptyList_whenNodeIsNull() {
        List<LinkDto> result = graphService.mapDependencies(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void mapDependencies_returnsEmptyList_whenDependenciesAreNull() {
        graphService.setGraphData(new DTO());

        ModNode node = new ModNode("jei", "1.0.0", null, Collections.emptySet());

        List<LinkDto> result = graphService.mapDependencies(node);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void mapDependencies_returnsEmptyList_whenDependenciesAreEmpty() {
        graphService.setGraphData(new DTO());

        ModNode node = new ModNode("jei", "1.0.0", Collections.emptySet(), Collections.emptySet());

        List<LinkDto> result = graphService.mapDependencies(node);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void mapDependencies_mapsAllDependencies() {
        graphService.setGraphData(new DTO());

        ModNode node = new ModNode(
                "jei",
                "1.0.0",
                Set.of("forge@47.0.0", "cloth-config@8.0.0"),
                Collections.emptySet()
        );

        List<LinkDto> result = graphService.mapDependencies(node);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(link ->
                link.getSource().equals("jei@1.0.0")
                        && link.getTarget().equals("forge@47.0.0")
                        && link.getRel().equals("required")
        ));
        assertTrue(result.stream().anyMatch(link ->
                link.getSource().equals("jei@1.0.0")
                        && link.getTarget().equals("cloth-config@8.0.0")
                        && link.getRel().equals("required")
        ));
    }

    @Test
    void mapConflicts_returnsEmptyList_whenNodeIsNull() {
        List<LinkDto> result = graphService.mapConflicts(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void mapConflicts_returnsEmptyList_whenConflictsAreNull() {
        graphService.setGraphData(new DTO());

        ModNode node = new ModNode("jei", "1.0.0", Collections.emptySet(), null);

        List<LinkDto> result = graphService.mapConflicts(node);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void mapConflicts_returnsEmptyList_whenConflictsAreEmpty() {
        graphService.setGraphData(new DTO());

        ModNode node = new ModNode("jei", "1.0.0", Collections.emptySet(), Collections.emptySet());

        List<LinkDto> result = graphService.mapConflicts(node);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void mapConflicts_mapsAllConflicts() {
        graphService.setGraphData(new DTO());

        ModNode node = new ModNode(
                "jei",
                "1.0.0",
                Collections.emptySet(),
                Set.of("optifine@1.2.3", "rubidium@0.6.0")
        );

        List<LinkDto> result = graphService.mapConflicts(node);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(link ->
                link.getSource().equals("jei@1.0.0")
                        && link.getTarget().equals("optifine@1.2.3")
                        && link.getRel().equals("conflicts")
        ));
        assertTrue(result.stream().anyMatch(link ->
                link.getSource().equals("jei@1.0.0")
                        && link.getTarget().equals("rubidium@0.6.0")
                        && link.getRel().equals("conflicts")
        ));
    }

    @Test
    void setGraphData_and_getGraphData_workCorrectly() {
        Mod dependency = new Mod();
        dependency.setId("forge");
        dependency.setVersion("47.0.0");
        dependency.setDepends(Collections.emptyList());
        dependency.setConflicts(Collections.emptyList());

        Mod conflict = new Mod();
        conflict.setId("optifine");
        conflict.setVersion("1.2.3");
        conflict.setDepends(Collections.emptyList());
        conflict.setConflicts(Collections.emptyList());

        Mod mainMod = new Mod();
        mainMod.setId("jei");
        mainMod.setVersion("1.0.0");
        mainMod.setDepends(List.of(dependency));
        mainMod.setConflicts(List.of(conflict));

        DTO dto = DTO.builder()
                .mods(List.of(mainMod))
                .build();

        graphService.setGraphData(dto);
        GraphResponseDto result = graphService.getGraphData();

        assertNotNull(result);
        assertNotNull(result.getNodes());
        assertNotNull(result.getLinks());

        assertEquals(1, result.getNodes().size());
        assertEquals("jei@1.0.0", result.getNodes().get(0).getId());
        assertEquals("mod", result.getNodes().get(0).getType());

        assertEquals(2, result.getLinks().size());
        assertTrue(result.getLinks().stream().anyMatch(link ->
                link.getSource().equals("jei@1.0.0")
                        && link.getTarget().equals("forge@47.0.0")
                        && link.getRel().equals("required")
        ));
        assertTrue(result.getLinks().stream().anyMatch(link ->
                link.getSource().equals("jei@1.0.0")
                        && link.getTarget().equals("optifine@1.2.3")
                        && link.getRel().equals("conflicts")
        ));
    }

    @Test
    void getGraphData_returnsEmptyLists_whenDtoHasNoMods() {
        DTO dto = DTO.builder()
                .mods(Collections.emptyList())
                .build();

        graphService.setGraphData(dto);
        GraphResponseDto result = graphService.getGraphData();

        assertNotNull(result);
        assertNotNull(result.getNodes());
        assertNotNull(result.getLinks());
        assertTrue(result.getNodes().isEmpty());
        assertTrue(result.getLinks().isEmpty());
    }
}