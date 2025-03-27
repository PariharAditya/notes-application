package org.note.notesapplication.Service;

import org.note.notesapplication.model.FileMessage;
import org.note.notesapplication.model.userResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class FileConsumerService {

    @Autowired
    private notesService notes;

    @KafkaListener(topics = "notes-content-topic", groupId = "notes-group")
    public void consumeFileContent(FileMessage file) {
        userResponse response = new userResponse();
        response.setContent(file.getContent());
        response.setTitle(file.getTitle());
        System.out.println("Consumed message: " + file);

        notes.saveNotes(file.getUsername(), response);
    }
}