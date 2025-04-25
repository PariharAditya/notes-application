package org.note.notesapplication.Service;

import lombok.extern.slf4j.Slf4j;
import org.note.notesapplication.Entity.User;
import org.note.notesapplication.DTO.UserRegistrationDto;  // Use DTO instead of model class
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
@Slf4j
public class UserRegistrationConsumer {

    @Autowired
    private MongoTemplate mongoTemplate;


    @KafkaListener(topics = "user-registration-topic", groupId = "user-registration-group",
            containerFactory = "userKafkaListenerContainerFactory")
    public void receiveUserRegistrationEvent(UserRegistrationDto userDto) {  // Changed parameter type
        log.info("Received user registration event for user: {}", userDto.getEmail());

        try {
            // Check if user already exists by keycloakId
            User existingUser = mongoTemplate.findOne(
                    Query.query(Criteria.where("keycloakId").is(userDto.getId())),
                    User.class);  // Using local entity class

            if (existingUser == null && userDto.getEmail() != null) {
                // Check by email as fallback
                existingUser = mongoTemplate.findOne(
                        Query.query(Criteria.where("email").is(userDto.getEmail())),
                        User.class);
            }

            if (existingUser == null) {
                // Create new user from DTO
                User newUser = new User();
                newUser.setUsername(userDto.getUsername() != null ?
                        userDto.getUsername() : userDto.getEmail());
                newUser.setEmail(userDto.getEmail());
                newUser.setFirstName(userDto.getFirstName());
                newUser.setLastName(userDto.getLastName());
                newUser.setKeycloakId(userDto.getId());
                newUser.setRegistrationDate(LocalDateTime.now());
                newUser.setNotes(new ArrayList<>());

                mongoTemplate.save(newUser);
                log.info("Created new user in database: {}", userDto.getEmail());
            } else {
                // Update existing user if needed
                boolean updated = false;

                if (existingUser.getKeycloakId() == null && userDto.getId() != null) {
                    existingUser.setKeycloakId(userDto.getId());
                    updated = true;
                }

                if (shouldUpdateName(existingUser, userDto)) {
                    existingUser.setFirstName(userDto.getFirstName());
                    existingUser.setLastName(userDto.getLastName());
                    updated = true;
                }

                if (existingUser.getUsername() == null && userDto.getUsername() != null) {
                    existingUser.setUsername(userDto.getUsername());
                    updated = true;
                }

                if (updated) {
                    mongoTemplate.save(existingUser);
                    log.info("Updated existing user: {}", userDto.getEmail());
                } else {
                    log.info("User already exists, no updates needed: {}", userDto.getEmail());
                }
            }
        } catch (Exception e) {
            log.error("Error processing user registration: {}", e.getMessage(), e);
        }
    }

    // Helper method to determine if name should be updated
    private boolean shouldUpdateName(User existingUser, UserRegistrationDto userDto) {
        // Only update if we're getting valid name data and existing name is missing or different
        boolean hasNewName = userDto.getFirstName() != null || userDto.getLastName() != null;
        boolean existingNameMissing = existingUser.getFirstName() == null || existingUser.getLastName() == null;
        boolean namesDiffer = !userDto.getFirstName().equals(existingUser.getFirstName()) ||
                !userDto.getLastName().equals(existingUser.getLastName());

        return hasNewName && (existingNameMissing || namesDiffer);
    }
}