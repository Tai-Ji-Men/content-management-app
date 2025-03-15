package com.tjm.contentManagementApp;

import com.tjm.contentManagementApp.common.GlobalContextLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ContentManagementAppApplication {
	public static void main(String[] args) {
		SpringApplication.run(ContentManagementAppApplication.class, args);
		GlobalContextLoader.loadContext();
	}
}
