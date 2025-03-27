package org.note.notesapplication.Controller;

import org.note.notesapplication.model.userResponse;
import org.note.notesapplication.Service.notesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/{username}/notes")
public class notesController {

    @Autowired
    private notesService notes;

    @PostMapping("/create")
    public ResponseEntity<userResponse> createNote(@PathVariable String username,
                                                 @RequestBody userResponse note) {
        userResponse saved = notes.saveNotes(username, note);
        return saved != null ?
            ResponseEntity.ok(saved) : ResponseEntity.badRequest().build();
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<userResponse>> getAllNotes(@PathVariable String username) {
        return ResponseEntity.ok(notes.getAllNotesByUser(username));
    }

    @GetMapping("/getSavedNotes/{title}")
    public ResponseEntity<userResponse> getNote(@PathVariable String username,
                                              @PathVariable String title) {
        userResponse note = notes.getNoteByTitleAndUser(username, title);
        return note != null ?
            ResponseEntity.ok(note) : ResponseEntity.notFound().build();
    }

    @PutMapping("/update/{title}")
    public ResponseEntity<userResponse> updateNote(@PathVariable String username,
                                                @PathVariable String title,
                                                @RequestBody userResponse updatedFields) {
        userResponse updated = notes.updateNotes(username, title, updatedFields);
        return updated != null ?
            ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/delete/{title}")
    public ResponseEntity<String> deleteNote(@PathVariable String username,
                                          @PathVariable String title) {
        userResponse deleted = notes.deleteNote(username, title);
        return deleted != null ?
            ResponseEntity.ok("Note deleted successfully") :
            ResponseEntity.notFound().build();
    }
}