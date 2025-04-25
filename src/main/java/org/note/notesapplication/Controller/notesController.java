package org.note.notesapplication.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.note.notesapplication.DTO.userResponse;
import org.note.notesapplication.Service.NotesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notes")
@Tag(name = "Notes", description = "Notes endpoints")
public class notesController {

    @Autowired
    private NotesService notes;

    @PostMapping
    @Operation(summary = "Create a new note",
            description = "This endpoint allows you to create a new note. Authentication required.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Note created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input or user not found")
            })
    public ResponseEntity<userResponse> createNote(@RequestBody userResponse note) {
        try {
            // Log authentication details
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("Auth principal: " + auth.getPrincipal().getClass().getName());
            System.out.println("Auth name: " + auth.getName());

            userResponse saved = notes.saveNotes(note);
            return saved != null ? ResponseEntity.ok(saved) : ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("Error in createNote: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    @Operation(summary = "Get all notes",
            description = "This endpoint retrieves all notes for the authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Notes retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "No notes available")
            })
    public ResponseEntity<List<userResponse>> getAllNotes() {
        return ResponseEntity.ok(notes.getAllNotesByUser());
    }

    @GetMapping("/{title}")
    @Operation(summary = "Get note by title",
            description = "Retrieves a specific note by title for the authenticated user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Note found and returned successfully"),
                    @ApiResponse(responseCode = "404", description = "Note not found")
            })
    public ResponseEntity<userResponse> getNote(@PathVariable String title) {
        userResponse note = notes.getNoteByTitleAndUser(title);
        return note != null ? ResponseEntity.ok(note) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{title}")
    @Operation(summary = "Update note",
            description = "Updates an existing note by title for the authenticated user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Note updated successfully"),
                    @ApiResponse(responseCode = "404", description = "Note not found")
            })
    public ResponseEntity<userResponse> updateNote(
            @PathVariable String title,
            @RequestBody userResponse updatedFields) {
        userResponse updated = notes.updateNotes(title, updatedFields);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{title}")
    @Operation(summary = "Delete note",
            description = "Deletes a specific note by title for the authenticated user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Note deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Note not found")
            })
    public ResponseEntity<userResponse> deleteNote(@PathVariable String title) {
        userResponse deleted = notes.deleteNote(title);
        return deleted != null ? ResponseEntity.ok(deleted) : ResponseEntity.notFound().build();
    }

    @GetMapping("/by-date/{date}")
    @Operation(summary = "Get notes by date",
            description = "Retrieves all notes for the authenticated user on a specific date (yyyy-MM-dd)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Notes found and returned successfully"),
                    @ApiResponse(responseCode = "404", description = "No notes found for the given date")
            })
    public ResponseEntity<List<userResponse>> getNoteByDate(@PathVariable String date) {
        List<userResponse> note = notes.getNoteByDate(date);
        return note != null ? ResponseEntity.ok(note) : ResponseEntity.notFound().build();
    }
}