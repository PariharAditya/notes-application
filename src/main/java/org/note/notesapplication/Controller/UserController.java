package org.note.notesapplication.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.note.notesapplication.Entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@RequestMapping("/new-user")
@Tag(name = "User Registration and login", description = "PENDING User Management endpoints for registration and login")
public class UserController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "This endpoint allows you to register a new user. " +
                    "You need to provide the user details in the request body.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User registered successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input or username already exists")
            }
    )
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        // Check if username already exists
        User existingUser = mongoTemplate.findOne(
                Query.query(Criteria.where("username").is(user.getUsername())),
                User.class
        );

        if (existingUser != null) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        user.setNotes(new ArrayList<>());

        mongoTemplate.save(user);

        return ResponseEntity.ok("User registered successfully");
    }
}
