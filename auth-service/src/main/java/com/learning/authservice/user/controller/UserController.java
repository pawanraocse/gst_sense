package com.learning.authservice.user.controller;

import com.learning.authservice.user.dto.UserDto;
import com.learning.authservice.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for tenant user management.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Get all users.
     */
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers(
            @RequestParam(required = false) String status) {

        List<UserDto> users;
        if ("ACTIVE".equalsIgnoreCase(status)) {
            users = userService.getActiveUsers();
        } else {
            users = userService.getAllUsers();
        }
        return ResponseEntity.ok(users);
    }

    /**
     * Search users by name or email.
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserDto>> searchUsers(
            @RequestParam String q) {

        List<UserDto> users = userService.searchUsers(q);
        return ResponseEntity.ok(users);
    }

    /**
     * Get a specific user by ID.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable String userId) {
        return userService.getUserById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get user statistics.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getUserStats() {
        return ResponseEntity.ok(userService.getUserStatsMap());
    }

    /**
     * Disable a user.
     */
    @PostMapping("/{userId}/disable")
    public ResponseEntity<Void> disableUser(
            @PathVariable String userId,
            @RequestHeader("X-User-Id") String currentUserId) {

        log.info("User {} disabling user {}", currentUserId, userId);
        userService.disableUser(userId);
        return ResponseEntity.noContent().build();
    }
}
