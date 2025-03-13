package org.note.notesapplication.Entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "notes")
@Data
@NoArgsConstructor
public class Notes {

    @Id
    private String id;

    private String title;
    private String content;
    private String username;

    private LocalDateTime createdDate;
}
