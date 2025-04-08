package org.note.filehandlingservice.controller;


import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.note.filehandlingservice.service.FileHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

@RestController
@RequestMapping("/users/{username}/files")
@Tag(name = "File", description = "File Management endpoints", externalDocs = @ExternalDocumentation(url = "http://localhost:8080/swagger-ui/index.html"))
public class FileController {

    @Autowired
    private FileHandlingService fileHandlingService;

    @Operation(
            summary = "Upload a file",
            description = "Uploads a file and processes it",
            responses = {
                    @ApiResponse(responseCode = "200", description = "File uploaded successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid file format or empty file"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    @Parameters({
            @Parameter(
                    name = "username",
                    description = "Username of the user uploading the file",
                    required = true
            ),
            @Parameter(
                    name = "file",
                    description = "File to be uploaded",
                    required = true
            )
    })
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