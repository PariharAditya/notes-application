package org.note.notesapplication.Config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoDBConfig {

    private final String databaseName = "notes-applicationDB";

    @Bean
    public MongoClient mongoClient() {
        String connectionUri = "mongodb+srv://adityapariharparihar:pariharDB50@cluster0.bv50w.mongodb.net/notes-application?retryWrites=true&w=majority";
        System.out.println("MongoDB URI: " + connectionUri);
        System.out.println("Database name: " + databaseName);

        ConnectionString connectionString = new ConnectionString(connectionUri);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        return MongoClients.create(settings);
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), databaseName);
    }
}