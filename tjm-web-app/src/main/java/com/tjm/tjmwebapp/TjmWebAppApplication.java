package com.tjm.tjmwebapp;

import com.tjm.common.GlobalContextLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;

@SpringBootApplication(exclude = {MongoAutoConfiguration.class})
public class TjmWebAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(TjmWebAppApplication.class, args);
        GlobalContextLoader.loadContext();
    }
}
