package com.inso.MinecraftProject.service;

import com.inso.MinecraftProject.dto.DTO;
import com.inso.MinecraftProject.entity.Mod;
import org.springframework.http.ResponseEntity; // cambio: se usa para devolver 200, 429, 500, etc.
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam; // cambio: permite leer ?mode=ok|partial|fail429...
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays; // cambio: útil para crear listas rápidas en el modo partial
import java.util.Collections;

@RestController
public class DependencyController {

    @GetMapping("/api/dependencies/missing")
    public ResponseEntity<?> getMissingDependencies(
            @RequestParam(defaultValue = "ok") String mode) throws InterruptedException {
        // cambio:
        // antes el endpoint siempre devolvía el mismo DTO
        // ahora acepta un query param "mode" para simular distintos escenarios del issue
        // ejemplo:
        // /api/dependencies/missing?mode=ok
        // /api/dependencies/missing?mode=partial
        // /api/dependencies/missing?mode=fail429
        // /api/dependencies/missing?mode=fail500
        // /api/dependencies/missing?mode=timeout

        if ("fail429".equalsIgnoreCase(mode)) {
            // cambio:
            // simula rate limit del backend
            // esto cubre el caso de error 429 pedido en el issue
            return ResponseEntity.status(429).body("Rate limit simulated");
        }

        if ("fail500".equalsIgnoreCase(mode)) {
            // cambio:
            // simula error interno del servidor
            // esto cubre el caso 500 pedido en el issue
            return ResponseEntity.status(500).body("Server error simulated");
        }

        if ("timeout".equalsIgnoreCase(mode)) {
            // cambio:
            // simula que el backend tarda demasiado
            // no se cambia estructura ni DTO, solo se retrasa la respuesta
            Thread.sleep(10000);

            DTO timeoutResponse = DTO.builder()
                    .mods(Collections.emptyList())
                    .edges(Collections.emptyList())
                    .missingDependencies(Collections.emptyList())
                    .resolvedDependencies(Collections.emptyList())
                    .build();

            return ResponseEntity.ok(timeoutResponse);
        }

        if ("partial".equalsIgnoreCase(mode)) {
            // cambio:
            // este modo simula resultados parciales
            // hay 2 mods faltantes, pero solo 1 link resuelto
            // así se representa que "some dependencies have no resolved link"

            Mod fabricApi = Mod.builder()
                    .id("fabric-api")
                    .version("0.100.1")
                    .depends(Collections.emptyList())
                    .breaks(Collections.emptyList())
                    .suggests(Collections.emptyList())
                    .recommends(Collections.emptyList())
                    .conflicts(Collections.emptyList())
                    .build();

            Mod clothConfig = Mod.builder()
                    .id("cloth-config")
                    .version("unknown")
                    .depends(Collections.emptyList())
                    .breaks(Collections.emptyList())
                    .suggests(Collections.emptyList())
                    .recommends(Collections.emptyList())
                    .conflicts(Collections.emptyList())
                    .build();

            DTO partialResponse = DTO.builder()
                    .mods(Collections.emptyList())
                    .edges(Collections.emptyList())
                    .missingDependencies(Arrays.asList(fabricApi, clothConfig))
                    // solo 1 link -> eso deja el otro mod sin link resuelto
                    .resolvedDependencies(Collections.singletonList("https://modrinth.com/mod/fabric-api"))
                    .build();

            return ResponseEntity.ok(partialResponse);
        }

        // cambio:
        // modo normal / por defecto = "ok"
        // mantiene casi la misma lógica original, pero con un ejemplo resuelto
        Mod fabricApi = Mod.builder()
                .id("fabric-api")
                .version("0.100.1")
                .depends(Collections.emptyList())
                .breaks(Collections.emptyList())
                .suggests(Collections.emptyList())
                .recommends(Collections.emptyList())
                .conflicts(Collections.emptyList())
                .build();

        DTO normalResponse = DTO.builder()
                .mods(Collections.emptyList())
                .edges(Collections.emptyList())
                .missingDependencies(Collections.singletonList(fabricApi))
                .resolvedDependencies(Collections.singletonList("https://modrinth.com/mod/fabric-api"))
                .build();

        return ResponseEntity.ok(normalResponse);
    }
}
