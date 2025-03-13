package org.note.notesapplication.Controller;

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
@RequestMapping("/users")
public class UserController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @PostMapping("/register")
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
