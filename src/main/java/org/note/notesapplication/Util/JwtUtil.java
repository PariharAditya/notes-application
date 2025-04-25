package org.note.notesapplication.Util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Utility class for working with JWT (JSON Web Tokens) in the application.
 * <p>
 * This class provides methods to extract user information from JWT tokens stored in
 * the Spring Security context. JWT tokens are used for authentication and authorization
 * in OAuth2 security implementations.
 * <p>
 * Why this class exists:
 * - Centralizes JWT token operations in one place
 * - Provides easy access to common user information
 * - Simplifies security-related code throughout the application
 */
@Component
@Slf4j
public class JwtUtil {

    /**
     * Extracts the current authenticated user's username from the JWT token.
     * <p>
     * This method is useful when you need to identify which user is performing an action,
     * especially for auditing, logging, or personalizing the user experience.
     *
     * @return The username of the currently authenticated user
     * @throws SecurityException if user is not authenticated or token is invalid
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("Authentication class: {}", authentication != null ? authentication.getClass().getName() : "null");

        if (authentication == null) {
            log.error("No authentication found in security context");
            throw new SecurityException("Not authenticated or invalid token");
        }

        Object principal = authentication.getPrincipal();
        log.info("Principal class: {}", principal != null ? principal.getClass().getName() : "null");

        // Use instanceof with pattern matching (available in Java 16+)
        if (principal instanceof Jwt jwt) {
            try {
                String username = jwt.getClaimAsString("preferred_username");
                log.info("Extracted username from JWT: {}", username);
                if (username == null || username.isEmpty()) {
                    username = jwt.getClaimAsString("email");
                    log.info("Using email as username: {}", username);
                }
                return username;
            } catch (Exception e) {
                log.error("Error extracting username from JWT: {}", e.getMessage(), e);
                throw new SecurityException("Invalid JWT token format");
            }
        } else {
            log.warn("Principal is not a JWT, it's: {}", principal.getClass().getName());
        }

        // Fallback to getName() if not a JWT
        String name = authentication.getName();
        log.info("Using authentication name as fallback: {}", name);
        if (name != null && !name.equals("anonymousUser")) {
            return name;
        }

        throw new SecurityException("Not authenticated or invalid token");
    }

    /**
     * Extracts the current authenticated user's ID from the JWT token.
     * <p>
     * This method is essential when you need to link user actions to their unique ID
     * in the database, such as when creating records or applying user-specific filters.
     *
     * @return The unique ID of the currently authenticated user
     * @throws SecurityException if user is not authenticated or token is invalid
     */
    public String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) auth.getPrincipal();
            return jwt.getSubject();
        }
        throw new SecurityException("Not authenticated or invalid token");
    }

    /**
     * Checks if the current user has a specific role.
     * <p>
     * This method is important for implementing role-based access control (RBAC),
     * allowing the application to make authorization decisions based on user roles.
     * It helps control what features or data a user can access.
     *
     * @param role The role to check
     * @return true if the user has the specified role, false otherwise
     */
    public boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaimAsMap("realm_access").get("roles") != null &&
                    ((List<String>) jwt.getClaimAsMap("realm_access").get("roles")).contains(role);
        }
        return false;
    }
}