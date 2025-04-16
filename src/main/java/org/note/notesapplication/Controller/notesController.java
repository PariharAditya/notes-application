package org.note.notesapplication.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.note.notesapplication.Service.NotesService;
import org.note.notesapplication.DTO.userResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/{username}/notes")
@Tag(name = "Notes", description = "Notes Management endpoints")
public class notesController {

        @Autowired
        private NotesService notes;

        @PostMapping("/create")
        @Operation(summary = "Create a new note",

                        description = "This endpoint allows you to create a new note for a specific user. " +
                                        "You need to provide the username in the URL and the note details in the request body.",

                        responses = {
                                        @ApiResponse(responseCode = "200", description = "Note created successfully"),
                                        @ApiResponse(responseCode = "400", description = "Invalid input or user not found"),
                                        @ApiResponse(responseCode = "404", description = "user not found"),
                        })
        @Parameters({
                        @Parameter(name = "username", description = "Username of the user creating the note"),
                        @Parameter(name = "note", description = "Details of the note to be created with title and content")
        })
        public ResponseEntity<userResponse> createNote(@PathVariable String username,
                        @RequestBody userResponse note) {
                userResponse saved = notes.saveNotes(username, note);
                return saved != null ? ResponseEntity.ok(saved) : ResponseEntity.badRequest().build();
        }

        @GetMapping("/getAll")
        @Operation(summary = "Get all notes by username", description = "This endpoint retrieves all notes for a specific user. "
                        +
                        "You need to provide the username in the URL.",

                        responses = {
                                        @ApiResponse(responseCode = "200", description = "Notes retrieved successfully"),
                                        @ApiResponse(responseCode = "404", description = "User not found or no notes available")
                        })
        @Parameter(name = "username", description = "Username of the user whose notes are to be retrieved")
        public ResponseEntity<List<userResponse>> getAllNotes(@PathVariable String username) {
                return ResponseEntity.ok(notes.getAllNotesByUser(username));
        }

        @GetMapping("/getSavedNotes/{title}")
        @Operation(summary = "Get note by title", description = "Retrieves a specific note by title for a user",

                        responses = {
                                        @ApiResponse(responseCode = "200", description = "Note found and returned successfully"),
                                        @ApiResponse(responseCode = "404", description = "Note not found")
                        })
        public ResponseEntity<userResponse> getNote(@PathVariable String username,
                        @PathVariable String title) {
                userResponse note = notes.getNoteByTitleAndUser(username, title);
                return note != null ? ResponseEntity.ok(note) : ResponseEntity.notFound().build();
        }

        @PutMapping("/update/{title}")
        @Operation(summary = "Update note", description = "Updates an existing note by title for a user",

                        responses = {
                                        @ApiResponse(responseCode = "200", description = "Note updated successfully"),
                                        @ApiResponse(responseCode = "404", description = "Note not found")
                        })
        public ResponseEntity<userResponse> updateNote(@PathVariable String username,
                        @PathVariable String title,
                        @RequestBody userResponse updatedFields) {
                userResponse updated = notes.updateNotes(username, title, updatedFields);
                return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
        }

        @DeleteMapping("/delete/{title}")
        @Operation(summary = "Delete note", description = "Deletes a specific note by title for a user",

                        responses = {
                                        @ApiResponse(responseCode = "200", description = "Note deleted successfully"),
                                        @ApiResponse(responseCode = "404", description = "Note not found")
                        })
        public ResponseEntity<String> deleteNote(@PathVariable String username,
                        @PathVariable String title) {
                userResponse deleted = notes.deleteNote(username, title);
                return deleted != null ? ResponseEntity.ok("Note deleted successfully")
                                : ResponseEntity.notFound().build();
        }

        @GetMapping("/getSavedNotesByDate/{date}")
        @Operation(summary = "Get notes by date", description = "Retrieves all notes for a user on a specific date",

                        responses = {
                                        @ApiResponse(responseCode = "200", description = "Notes found and returned successfully"),
                                        @ApiResponse(responseCode = "404", description = "No notes found for the given date")
                        })
        public ResponseEntity<List<userResponse>> getNoteByDate(@PathVariable String username,
                        @PathVariable String date) {
                List<userResponse> note = notes.getNoteByDate(username, date);
                return note != null ? ResponseEntity.ok(note) : ResponseEntity.notFound().build();
        }
}