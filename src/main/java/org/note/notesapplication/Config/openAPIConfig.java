package org.note.notesapplication.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class openAPIConfig {

    @Bean
    public OpenAPI openAPI() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080");
        localServer.setDescription("Local Development");

        Server gatewayServer = new Server();
        gatewayServer.setUrl("http://localhost:8181");
        gatewayServer.setDescription("API Gateway");

        return new OpenAPI()
            .servers(List.of(localServer, gatewayServer))
            .info(new Info()
                .title("Notes Application API")
                .description("API Documentation for Notes Application")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Aditya Parihar")
                    .email("adityapariharparihar@gmail.com")));
    }
}