package org.note.filehandlingservice.controller;


import org.note.filehandlingservice.service.FileHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

@RestController
@RequestMapping("/users/{username}/files")
public class FileController {

    @Autowired
    private FileHandlingService fileHandlingService;


    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@PathVariable String username,
                                           @RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please upload a non-empty file");
        }

        if (!Objects.requireNonNull(file.getOriginalFilename()).toLowerCase().endsWith(".txt")) {
            return ResponseEntity.badRequest().body("Only .txt files are supported");
        }

        boolean processed = fileHandlingService.processFile(username, file);
        return processed ?
                ResponseEntity.ok("File uploaded and queued for processing") :
                ResponseEntity.internalServerError().body("Error processing file");
    }
}