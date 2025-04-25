package org.note.apigateway.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class LoginRequestDto implements Serializable {
    private String username;
    private String password;
}