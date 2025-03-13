package org.note.notesapplication.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class userResponse {
    private String title;
    private String content;

    private LocalDateTime createdDate;
    private String formattedDate;
}
