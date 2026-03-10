package com.inso.MinecraftProject;

import com.inso.MinecraftProject.view.MissingDependencyItem;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
@Controller

public class MainController {
   @GetMapping("/missing-dependencies")
    public String missingDependencies(Model model) {
        List<MissingDependencyItem> missingDependencies = List.of(
                new MissingDependencyItem(
                        "Fabric API",
                        "1.20.1",
                        "Fabric",
                        "/r?u=https://modrinth.com/mod/fabric-api",
                        true
                ),
                new MissingDependencyItem(
                        "Cloth Config",
                        "1.20.1",
                        "Fabric",
                        "/r?u=https://modrinth.com/mod/cloth-config",
                        true
                ),
                new MissingDependencyItem(
                        "Unknown Dependency",
                        "1.20.1",
                        "Fabric",
                        null,
                        false
                )
        );

        model.addAttribute("missingDependencies", missingDependencies);
        model.addAttribute("analysisHasPartialResults", true);
        model.addAttribute("loader", "Fabric");
        model.addAttribute("minecraftVersion", "1.20.1");

        return "missing-dependencies";
    }
}
