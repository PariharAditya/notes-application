package org.note.notesapplication.Service;

import org.note.notesapplication.DTO.FileMessage;
import org.note.notesapplication.DTO.userResponse;
import org.note.notesapplication.Util.JwtUtil;
import org.note.notesapplication.Util.NoteCreationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/*
* It's trying to call a method with two parameters (username, response)
* but saveNotes now only accepts one parameter (response)
* In a Kafka consumer context,
* there's no authenticated user in the security context since Kafka consumers run in background threads without an HTTP request
*/

@Service
public class FileConsumerService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private JwtUtil util;

    @Autowired
    private NoteCreationUtil notesUtil;

    @KafkaListener(topics = "notes-content-topic", groupId = "notes-group")
    public void consumeFileContent(FileMessage file) {
        userResponse response = new userResponse();
        response.setContent(file.getContent());
        response.setTitle(file.getTitle());
        System.out.println("Consumed message: " + file);

        // Use direct MongoDB operations since we can't use JWT in Kafka context
        saveNoteForUser(file.getUsername(), response);
    }

    private void saveNoteForUser(String username, userResponse response) {
        // Find user
        notesUtil.createNoteForUser(username, response);
    }
}