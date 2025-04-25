package org.note.notesapplication.Service;

import lombok.extern.slf4j.Slf4j;
import org.note.notesapplication.Entity.Notes;
import org.note.notesapplication.Entity.User;
import org.note.notesapplication.DTO.userResponse;
import org.note.notesapplication.Util.JwtUtil;
import org.note.notesapplication.Util.NoteCreationUtil;
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
 * keycloak is implemented 16-04-2025
 * so we are using username
 * without keycloak we can use session handling, UserIdnetificationFilter
 * above way not secure just for isolation
 */
@Service
@Slf4j
public class NotesService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private JwtUtil util;

    @Autowired
    private NoteCreationUtil notesUtil;

    // Create note for a specific user
    public userResponse saveNotes(userResponse noteDto) {
        try {
            // Get username from security context
            String username = util.getCurrentUsername();
            log.debug("Saving note for user: {}", username);

            // Convert DTO to entity
            Notes note = new Notes();
            note.setTitle(noteDto.getTitle());
            note.setContent(noteDto.getContent());
            note.setUsername(username);
            note.setCreatedDate(LocalDateTime.now());

            // Save the note entity
            Notes savedNote = mongoTemplate.save(note);

            // Update user's notes list
            User user = mongoTemplate.findOne(
                    Query.query(Criteria.where("username").is(username)),
                    User.class);

            if (user != null) {
                user.getNotes().add(savedNote);
                mongoTemplate.save(user);
            } else {
                log.warn("User not found in database: {}", username);
            }

            return convertToDTO(savedNote);
        } catch (Exception e) {
            log.error("Error saving note: {}", e.getMessage(), e);
            return null;
        }
    }

    // Get all notes for a user
    public List<userResponse> getAllNotesByUser() {
        String username = util.getCurrentUsername();

        List<Notes> notes = mongoTemplate.find(
                Query.query(Criteria.where("username").is(username)),
                Notes.class);
        return notes.stream().map(this::convertToDTO).toList();
    }

    // Get a specific note by title for a user
    public userResponse getNoteByTitleAndUser(String title) {
        String username = util.getCurrentUsername();
        Notes note = mongoTemplate.findOne(
                Query.query(Criteria.where("username").is(username)
                        .and("title").is(title)),
                Notes.class);
        return note != null ? convertToDTO(note) : null;
    }

    // Update note for a user
    public userResponse updateNotes(String title, userResponse updatedFields) {
        String username = util.getCurrentUsername();
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
    public userResponse deleteNote(String title) {
        String username = util.getCurrentUsername();
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

    public List<userResponse> getNoteByDate(String datestr) {
        if (datestr == null || datestr.isEmpty())
            return null;
        String username = util.getCurrentUsername();
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
        return notesUtil.convertToDTO(note);
    }
}