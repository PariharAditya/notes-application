package org.note.notesapplication.DTO;

import lombok.Data;

@Data
public class SmsRequest {
    private String toNumber;
    private String message;
}