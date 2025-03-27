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

        String filename = file.getOriginalFilename();
        String content = readFileContent(file);
        log.info("{} : {}", filename, content);


        //TODO for now kept simple later we've Other validation checks Pending

        if (filename != null && !filename.isEmpty()) {
            FileMessage fileMessage = new FileMessage();
            fileMessage.setTitle(fileMessage.getTitle());
            fileMessage.setContent(fileMessage.getContent());
            fileMessage.setUsername(userName);

            System.out.println("Sending message: " + fileMessage);

            kafkaTemplate.send(notesTopic, fileMessage);
        }

        return true;
    }

    public String readFileContent(MultipartFile file) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
        return reader.lines().collect(Collectors.joining("\n"));
    }

}
