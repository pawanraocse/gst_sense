package com.learning.authservice.invitation.controller;

import com.learning.authservice.invitation.dto.InvitationRequest;
import com.learning.authservice.invitation.dto.InvitationResponse;
import com.learning.authservice.invitation.service.InvitationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Controller for managing invitations.
 * Tenant context removed.
 */
@RestController
@RequestMapping("/api/v1/invitations")
@RequiredArgsConstructor
@Slf4j
public class InvitationController {

    private final InvitationService invitationService;

    @PostMapping

    public ResponseEntity<InvitationResponse> createInvitation(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody InvitationRequest request) {

        InvitationResponse response = invitationService.createInvitation(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping

    public ResponseEntity<List<InvitationResponse>> getInvitations() {
        return ResponseEntity.ok(invitationService.getInvitations());
    }

    @DeleteMapping("/{id}")

    public ResponseEntity<Void> revokeInvitation(@PathVariable UUID id) {
        invitationService.revokeInvitation(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/resend")

    public ResponseEntity<Void> resendInvitation(@PathVariable UUID id) {
        invitationService.resendInvitation(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Validate an invitation token.
     * Public endpoint - called before user has an account.
     * Requires tenant parameter to route to correct database.
     */
    @GetMapping("/validate")
    public ResponseEntity<InvitationValidationResponse> validateInvitation(
            @RequestParam String token) {
        var invitation = invitationService.validateInvitation(token);
        return ResponseEntity.ok(new InvitationValidationResponse(
                invitation.getEmail(),
                invitation.getRoleId(),
                true));
    }

    /**
     * Accept an invitation and create user account.
     * Public endpoint - called during join flow.
     * Requires tenant parameter to route to correct database.
     */
    @PostMapping("/accept")
    public ResponseEntity<Void> acceptInvitation(@RequestBody AcceptInvitationRequest request) {
        invitationService.acceptInvitation(request.token(), request.password(), request.name());
        return ResponseEntity.ok().build();
    }

    public record InvitationValidationResponse(String email, String roleId, boolean valid) {
    }

    public record AcceptInvitationRequest(String token, String password, String name) {
    }
}
