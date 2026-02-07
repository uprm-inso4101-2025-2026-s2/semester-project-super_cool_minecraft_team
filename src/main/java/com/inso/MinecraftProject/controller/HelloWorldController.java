package com.inso.MinecraftProject.controller;

import com.inso.MinecraftProject.service.HelloWorldService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloWorldController {
    // Constructor-based dependency injection of the HelloWorldService
    private final HelloWorldService helloWorldService;

    // The constructor is used to inject the HelloWorldService into the controller
    HelloWorldController(HelloWorldService helloWorldService) {
        this.helloWorldService = helloWorldService;
    }

    // This method will handle GET requests to the "/hello" endpoint
    // It returns the name of the view that should be rendered when this endpoint is accessed.
    @GetMapping("/hello")
    public String sayHello(Model model) {
        // Return the name of the view (e.g., a Thymeleaf template) to be rendered
        // The model is used to pass data to the view. In this case, we are adding a message to the model that can be displayed in the view.
        String message = helloWorldService.getMessage();
        // The message is calculated on the service layer. This allows the view to access this data and display it to the user.
        model.addAttribute("message", message);
        // The view (e.g., hello.html) can access the "message" attribute and display it to the user.
        return "hello"; // This should correspond to a template named "hello.html"
    }

}
