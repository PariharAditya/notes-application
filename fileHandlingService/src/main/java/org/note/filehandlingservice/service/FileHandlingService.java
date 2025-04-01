package org.note.filehandlingservice.service;

import lombok.extern.slf4j.Slf4j;
import org.note.filehandlingservice.model.FileMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FileHandlingService {

    @Autowired
    private KafkaTemplate<String, FileMessage> kafkaTemplate;

    @Value("${app.kafka.notes-topic}")
    private String notesTopic;

    public boolean processFile(String userName, MultipartFile file) throws IOException {
        // Validate input
        if (file.isEmpty() || userName == null) {
            log.error("File is empty or username is null");
            return false;
        }

        // Read file content
        String dataFromFile = readFileContent(file);
        log.info("File content: {}", dataFromFile);

        // Parse file content
        FileMessage fileMessage = parseFileContent(dataFromFile, userName);

        kafkaTemplate.send(notesTopic, fileMessage);
        log.info("Message sent to Kafka: {}", fileMessage);

        return true;

    }

    private FileMessage parseFileContent(String fileContent, String userName) {
        // Normalize line breaks and trim
        fileContent = fileContent.trim().replaceAll("\r\n", "\n");

        // Split content into lines
        String[] lines = fileContent.split("\n");

        // Default values
        String title = "Untitled";
        StringBuilder content = new StringBuilder();

        // Parse title and content
        for (String line : lines) {
            line = line.trim().toLowerCase();
            if (line.startsWith("title:")) {
                title = line.substring(6).trim();
            } else if (line.startsWith("content:")) {
                content.append(line.substring(8).trim());
            } else if (!line.startsWith("title:") && !line.startsWith("content:")) {
                content.append(line).append("\n");
            }
        }

        // Create and return FileMessage
        FileMessage fileMessage = new FileMessage();
        fileMessage.setTitle(title);
        fileMessage.setContent(content.toString().trim());
        fileMessage.setUsername(userName);

        return fileMessage;
    }

    public String readFileContent(MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}