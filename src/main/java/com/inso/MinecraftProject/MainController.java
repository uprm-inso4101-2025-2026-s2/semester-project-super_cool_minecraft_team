package com.inso.MinecraftProject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/missing-dependencies")
    public String missingDependencies(Model model) {
        model.addAttribute("loader", "Fabric");
        model.addAttribute("minecraftVersion", "1.20.1");
        return "missing-dependencies";
    }
}
