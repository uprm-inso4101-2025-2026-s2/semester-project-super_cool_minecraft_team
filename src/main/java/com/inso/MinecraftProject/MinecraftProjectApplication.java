package com.inso.MinecraftProject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MinecraftProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(MinecraftProjectApplication.class, args);
	}

}
