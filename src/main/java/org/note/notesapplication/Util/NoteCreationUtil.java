package org.note.notesapplication.Util;

import org.note.notesapplication.DTO.userResponse;
import org.note.notesapplication.Entity.Notes;
import org.note.notesapplication.Entity.User;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class NoteCreationUtil {

    private final MongoTemplate mongoTemplate;

    public NoteCreationUtil(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public userResponse createNoteForUser(String username, userResponse noteData) {
        // Find user
        User user = mongoTemplate.findOne(
                Query.query(Criteria.where("username").is(username)),
                User.class);
        if (user == null)
            return null;

        // Create note
        Notes note = new Notes();
        note.setTitle(noteData.getTitle());
        note.setContent(noteData.getContent());
        note.setUsername(username);
        note.setCreatedDate(LocalDateTime.now());

        Notes savedNote = mongoTemplate.save(note);

        // Add note to user's list
        user.getNotes().add(savedNote);
        mongoTemplate.save(user);

        return convertToDTO(savedNote);
    }

    public userResponse convertToDTO(Notes note) {
        userResponse response = new userResponse();
        response.setTitle(note.getTitle());
        response.setContent(note.getContent());
        response.setCreatedDate(note.getCreatedDate());

        // Handle null dates
        if (note.getCreatedDate() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            response.setFormattedDate(note.getCreatedDate().format(formatter));
        } else {
            response.setFormattedDate("No date available");
        }

        return response;
    }
}