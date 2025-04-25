package org.note.notesapplication.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class UserRegistrationDto implements Serializable {
    private String id;          // Keycloak ID
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDateTime registrationDate;
}
