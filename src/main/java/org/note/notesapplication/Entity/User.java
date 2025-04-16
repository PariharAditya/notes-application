package org.note.notesapplication.Entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "users")
@Data
@NoArgsConstructor
public class User {

    @Id
    private String id;

    private String password;
    private String keycloakId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDateTime registrationDate;

    @DBRef
    private List<Notes> notes = new ArrayList<>();
}