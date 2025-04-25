package org.note.apigateway.service;

import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.note.apigateway.DTO.UserRegistrationDto;
import org.note.apigateway.DTO.UserResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
@Slf4j
public class KeyCloakService {

    @Autowired
    private KafkaTemplate<String, UserResponseDto> kafkaTemplate;

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String clientId;

    @Value("${keycloak.credentials.secret}")
    private String clientSecret;

    @Value("${app.kafka.user-registration-topic}")
    private String userRegistrationTopic;

    public UserResponseDto registerUser(UserRegistrationDto request) {
        try {
            // Create Keycloak client explicitly
            Keycloak keycloakClient = KeycloakBuilder.builder()
                    .serverUrl(authServerUrl)
                    .realm(realm)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .grantType("client_credentials")
                    .build();

            log.debug("Created Keycloak client with realm: {}, clientId: {}", realm, clientId);

            // 1. Create Keycloak user representation
            UserRepresentation keycloakUser = new UserRepresentation();
            keycloakUser.setUsername(request.getUsername());
            keycloakUser.setEmail(request.getEmail());
            keycloakUser.setFirstName(request.getFirstName());
            keycloakUser.setLastName(request.getLastName());
            keycloakUser.setEnabled(true);

            // 2. Set password
            CredentialRepresentation credential = createPasswordCredential(request.getPassword());
            keycloakUser.setCredentials(Collections.singletonList(credential));

            // 3. Create user in Keycloak
            Response response = keycloakClient.realm(realm).users().create(keycloakUser);
            log.info("Keycloak user creation response: {}", response.getStatus());

            if (response.getStatus() != 201) {
                throw new RuntimeException("Failed to create user in Keycloak: " + response.getStatusInfo().getReasonPhrase());
            }

            // 4. Get created user ID
            String userId = CreatedResponseUtil.getCreatedId(response);
            log.info("User created in Keycloak with ID: {}", userId);

            // 5. Assign default User role
            try {
                RoleRepresentation userRole = keycloakClient.realm(realm)
                        .roles().get("User").toRepresentation();

                keycloakClient.realm(realm)
                        .users().get(userId)
                        .roles().realmLevel()
                        .add(Collections.singletonList(userRole));


                log.info("USER role assigned to user: {}", userId);
            } catch (Exception e) {
                log.error("Failed to assign role to user: {}", e.getMessage(), e);
            }

            // 6. Create response DTO
            UserResponseDto userDto = new UserResponseDto();
            userDto.setId(userId);
            userDto.setUsername(request.getUsername());
            userDto.setEmail(request.getEmail());
            userDto.setFirstName(request.getFirstName());
            userDto.setLastName(request.getLastName());
            userDto.setRegistrationDate(LocalDateTime.now());

            // 7. Send to Kafka for MongoDB synchronization
            kafkaTemplate.send(userRegistrationTopic, userDto.getUsername(), userDto)
                    .thenAccept(result -> log.debug("User registration event sent to Kafka successfully: {}", result.getRecordMetadata()))
                    .exceptionally(ex -> {
                        log.error("Failed to send user Registration event to Kafka: {}", ex.getMessage(), ex);
                        return null;
                    });

            return userDto;
        } catch (Exception e) {
            log.error("User registration failed: {}", e.getMessage(), e);
            throw new RuntimeException("User registration failed: " + e.getMessage(), e);
        }
    }

    private CredentialRepresentation createPasswordCredential(String password) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(false);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        return credential;
    }
}