package org.note.notesapplication.Service;

import org.note.notesapplication.Entity.Notes;
import org.note.notesapplication.Entity.User;
import org.note.notesapplication.DTO.userResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/*
store notes of a user in a []
db would look like
id
username
password
[]-list of notes
for this we'll use DBRef to've linking between two table
*/

/*
 * for now(11th april 2025) keycloak is not implemented
 * so we are using username

 * without keycloak we can use session handling, UserIdnetificationFilter
 * above way not secure just for isolation
 */
@Service
public class NotesService {

    @Autowired
    private MongoTemplate mongoTemplate;

    // Create note for a specific user
    public userResponse saveNotes(String username, userResponse response) {
        // Find user
        User user = mongoTemplate.findOne(
                Query.query(Criteria.where("username").is(username)),
                User.class);
        if (user == null)
            return null;

        // Create note
        Notes note = new Notes();
        note.setTitle(response.getTitle());
        note.setContent(response.getContent());
        note.setUsername(username);
        note.setCreatedDate(LocalDateTime.now());

        Notes savedNote = mongoTemplate.save(note);

        // Add note to user's list
        user.getNotes().add(savedNote);
        mongoTemplate.save(user);

        return convertToDTO(savedNote);
    }

    // Get all notes for a user
    public List<userResponse> getAllNotesByUser(String username) {
        List<Notes> notes = mongoTemplate.find(
                Query.query(Criteria.where("username").is(username)),
                Notes.class);
        return notes.stream().map(this::convertToDTO).toList();
    }

    // Get a specific note by title for a user
    public userResponse getNoteByTitleAndUser(String username, String title) {
        Notes note = mongoTemplate.findOne(
                Query.query(Criteria.where("username").is(username)
                        .and("title").is(title)),
                Notes.class);
        return note != null ? convertToDTO(note) : null;
    }

    // Update note for a user
    public userResponse updateNotes(String username, String title, userResponse updatedFields) {
        Notes existingNote = mongoTemplate.findOne(
                Query.query(Criteria.where("username").is(username)
                        .and("title").is(title)),
                Notes.class);

        if (existingNote == null)
            return null;

        if (updatedFields.getContent() != null) {
            existingNote.setContent(updatedFields.getContent());
        }

        if (updatedFields.getTitle() != null && !updatedFields.getTitle().equals(title)) {
            existingNote.setTitle(updatedFields.getTitle());
        }

        Notes updatedNote = mongoTemplate.save(existingNote);
        return convertToDTO(updatedNote);
    }

    // Delete a note for a user
    public userResponse deleteNote(String username, String title) {
        Notes deletedNote = mongoTemplate.findAndRemove(
                Query.query(Criteria.where("username").is(username)
                        .and("title").is(title)),
                Notes.class);

        if (deletedNote != null) {
            // Remove reference from user's list
            User user = mongoTemplate.findOne(
                    Query.query(Criteria.where("username").is(username)),
                    User.class);
            if (user != null) {
                user.getNotes().removeIf(note -> note.getId().equals(deletedNote.getId()));
                mongoTemplate.save(user);
            }
            return convertToDTO(deletedNote);
        }

        return null;
    }

    public List<userResponse> getNoteByDate(String username, String datestr) {
        if (datestr == null || datestr.isEmpty())
            return null;
        System.out.println(username + " " + datestr);
        try {
            LocalDate date = LocalDate.parse(datestr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDateTime startDate = date.atStartOfDay();
            LocalDateTime endDate = date.atTime(23, 59, 59);

            List<Notes> notes = mongoTemplate.find(
                    Query.query(Criteria.where("username").is(username)
                            .and("createdDate").gte(startDate).lte(endDate)),
                    Notes.class);
            return notes.stream().map(this::convertToDTO).toList();
        } catch (DateTimeParseException e) {
            // Handle invalid date format
            System.out.println("Invalid date format: " + e.getMessage());
            return List.of(); // or throw an exception
        }
    }

    // Helper method
    private userResponse convertToDTO(Notes note) {
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