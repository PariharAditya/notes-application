package org.note.notesapplication;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.note.notesapplication.Entity.Notes;
import org.note.notesapplication.Entity.User;
import org.note.notesapplication.model.userResponse;
import org.note.notesapplication.Service.notesService;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class NotesServiceTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private notesService service;

    private User testUser;
    private Notes testNote;
    private userResponse testResponse;
    private final String TEST_USERNAME = "testUser";
    private final String TEST_TITLE = "Test Title";
    private final String TEST_CONTENT = "Test Content";

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setUsername(TEST_USERNAME);
        testUser.setNotes(new ArrayList<>());
        
        // Setup test note
        testNote = new Notes();
        testNote.setId("1");
        testNote.setUsername(TEST_USERNAME);
        testNote.setTitle(TEST_TITLE);
        testNote.setContent(TEST_CONTENT);
        testNote.setCreatedDate(LocalDateTime.now());
        
        // Setup test userResponse
        testResponse = new userResponse();
        testResponse.setTitle(TEST_TITLE);
        testResponse.setContent(TEST_CONTENT);
    }

    @Test
    void saveNotes_UserExists_ShouldReturnSavedNote() {
        // Arrange
        when(mongoTemplate.findOne(any(Query.class), eq(User.class))).thenReturn(testUser);
        when(mongoTemplate.save(any(Notes.class))).thenReturn(testNote);
        when(mongoTemplate.save(any(User.class))).thenReturn(testUser);
        
        // Act
        userResponse result = service.saveNotes(TEST_USERNAME, testResponse);
        
        // Assert
        assertNotNull(result);
        assertEquals(TEST_TITLE, result.getTitle());
        assertEquals(TEST_CONTENT, result.getContent());
        assertNotNull(result.getCreatedDate());
        assertNotNull(result.getFormattedDate());
        verify(mongoTemplate).save(any(Notes.class));
        verify(mongoTemplate).save(any(User.class));
    }

    @Test
    void saveNotes_UserDoesNotExist_ShouldReturnNull() {
        // Arrange
        when(mongoTemplate.findOne(any(Query.class), eq(User.class))).thenReturn(null);
        
        // Act
        userResponse result = service.saveNotes(TEST_USERNAME, testResponse);
        
        // Assert
        assertNull(result);
        verify(mongoTemplate, never()).save(any(Notes.class));
        verify(mongoTemplate, never()).save(any(User.class));
    }

    @Test
    void getAllNotesByUser_NotesExist_ShouldReturnList() {
        // Arrange
        List<Notes> notesList = Arrays.asList(testNote);
        when(mongoTemplate.find(any(Query.class), eq(Notes.class))).thenReturn(notesList);
        
        // Act
        List<userResponse> results = service.getAllNotesByUser(TEST_USERNAME);
        
        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(TEST_TITLE, results.get(0).getTitle());
        assertEquals(TEST_CONTENT, results.get(0).getContent());
    }

    @Test
    void getAllNotesByUser_NoNotes_ShouldReturnEmptyList() {
        // Arrange
        when(mongoTemplate.find(any(Query.class), eq(Notes.class))).thenReturn(new ArrayList<>());
        
        // Act
        List<userResponse> results = service.getAllNotesByUser(TEST_USERNAME);
        
        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void getNoteByTitleAndUser_NoteExists_ShouldReturnNote() {
        // Arrange
        when(mongoTemplate.findOne(any(Query.class), eq(Notes.class))).thenReturn(testNote);
        
        // Act
        userResponse result = service.getNoteByTitleAndUser(TEST_USERNAME, TEST_TITLE);
        
        // Assert
        assertNotNull(result);
        assertEquals(TEST_TITLE, result.getTitle());
        assertEquals(TEST_CONTENT, result.getContent());
    }

    @Test
    void getNoteByTitleAndUser_NoteDoesNotExist_ShouldReturnNull() {
        // Arrange
        when(mongoTemplate.findOne(any(Query.class), eq(Notes.class))).thenReturn(null);
        
        // Act
        userResponse result = service.getNoteByTitleAndUser(TEST_USERNAME, TEST_TITLE);
        
        // Assert
        assertNull(result);
    }

    @Test
    void updateNotes_NoteExists_ShouldReturnUpdatedNote() {
        // Arrange
        userResponse updatedFields = new userResponse();
        updatedFields.setTitle("New Title");
        updatedFields.setContent("Updated Content");
        
        when(mongoTemplate.findOne(any(Query.class), eq(Notes.class))).thenReturn(testNote);
        
        Notes updatedNote = new Notes();
        updatedNote.setId("1");
        updatedNote.setUsername(TEST_USERNAME);
        updatedNote.setTitle("New Title");
        updatedNote.setContent("Updated Content");
        updatedNote.setCreatedDate(testNote.getCreatedDate());
        
        when(mongoTemplate.save(any(Notes.class))).thenReturn(updatedNote);
        
        // Act
        userResponse result = service.updateNotes(TEST_USERNAME, TEST_TITLE, updatedFields);
        
        // Assert
        assertNotNull(result);
        assertEquals("New Title", result.getTitle());
        assertEquals("Updated Content", result.getContent());
    }

    @Test
    void updateNotes_NoteDoesNotExist_ShouldReturnNull() {
        // Arrange
        userResponse updatedFields = new userResponse();
        updatedFields.setContent("Updated Content");
        
        when(mongoTemplate.findOne(any(Query.class), eq(Notes.class))).thenReturn(null);
        
        // Act
        userResponse result = service.updateNotes(TEST_USERNAME, TEST_TITLE, updatedFields);
        
        // Assert
        assertNull(result);
        verify(mongoTemplate, never()).save(any(Notes.class));
    }

    @Test
    void deleteNote_NoteExists_ShouldReturnDeletedNote() {
        // Arrange
        when(mongoTemplate.findAndRemove(any(Query.class), eq(Notes.class))).thenReturn(testNote);
        when(mongoTemplate.findOne(any(Query.class), eq(User.class))).thenReturn(testUser);
        
        // Act
        userResponse result = service.deleteNote(TEST_USERNAME, TEST_TITLE);
        
        // Assert
        assertNotNull(result);
        assertEquals(TEST_TITLE, result.getTitle());
        assertEquals(TEST_CONTENT, result.getContent());
        verify(mongoTemplate).save(any(User.class));
    }

    @Test
    void deleteNote_NoteDoesNotExist_ShouldReturnNull() {
        // Arrange
        when(mongoTemplate.findAndRemove(any(Query.class), eq(Notes.class))).thenReturn(null);
        
        // Act
        userResponse result = service.deleteNote(TEST_USERNAME, TEST_TITLE);
        
        // Assert
        assertNull(result);
        verify(mongoTemplate, never()).findOne(any(Query.class), eq(User.class));
        verify(mongoTemplate, never()).save(any(User.class));
    }
}