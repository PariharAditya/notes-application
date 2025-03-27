package org.note.notesapplication.model;

import lombok.Data;

@Data
public class FileMessage {
    private String title;
    private String content;
    private String username;
}