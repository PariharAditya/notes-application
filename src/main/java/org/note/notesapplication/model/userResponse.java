package org.note.notesapplication.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class userResponse implements Serializable {
    private String title;
    private String content;

    private LocalDateTime createdDate;
    private String formattedDate;

    @Serial
    private static final long serialVersionUID = 1L;
}
