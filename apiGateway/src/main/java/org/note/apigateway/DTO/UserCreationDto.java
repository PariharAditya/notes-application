package org.note.apigateway.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserCreationDto {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
}
