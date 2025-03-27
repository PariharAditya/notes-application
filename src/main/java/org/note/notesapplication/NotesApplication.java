package org.note.notesapplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Collections;

@SpringBootApplication
@EnableMongoAuditing
@EnableCaching
@EnableScheduling
@EnableDiscoveryClient
public class NotesApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(NotesApplication.class);
        app.setDefaultProperties(Collections.singletonMap("spring.application.name", "NotesApplication"));
        app.run(args);
    }

}
