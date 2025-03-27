package org.note.notificationservice.model;

import lombok.Data;

@Data
public class SmsRequest {
    private String toNumber;
    private String message;
}