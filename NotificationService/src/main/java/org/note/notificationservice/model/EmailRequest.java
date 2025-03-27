package org.note.notificationservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {
    private String toEmail;
    private String subject;
    private String body;

    private byte[] content;
    private String fileName;
    private String mimeType;
}