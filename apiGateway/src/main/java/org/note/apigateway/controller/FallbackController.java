package org.note.apigateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/notesApplication")
    public String notesApplicationFallback() {
        return "Notes Application is currently unavailable. Please try again later.";
    }

    @GetMapping("/email")
    public String emailServiceFallback() {
        return "Email Service is currently unavailable. Please try again later.";
    }
}