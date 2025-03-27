package org.note.notesapplication.model;

import lombok.Data;

@Data
public class SmsRequest {
    private String toNumber;
    private String message;
}