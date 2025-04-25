package org.note.apigateway.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
public class UserResponseDto implements Serializable {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String keycloakId;
    private LocalDateTime registrationDate;
}
