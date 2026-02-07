package com.inso.MinecraftProject.service;

import org.springframework.stereotype.Service;

@Service
public class HelloWorldService {

    private String getRandomMessage() {
        String[] messages = {
            "Hello, Minecraft World!",
            "Welcome to the Minecraft Project!",
            "Have fun building and exploring!",
            "Craft your dreams in Minecraft!"
        };
        int randomIndex = (int) (Math.random() * messages.length);
        return messages[randomIndex];
    }

    public String getMessage() {
        return getRandomMessage();
    }
}
