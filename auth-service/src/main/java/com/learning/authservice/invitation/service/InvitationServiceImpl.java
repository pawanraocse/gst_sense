package com.learning.authservice.invitation.service;

import com.learning.authservice.config.CognitoProperties;
import com.learning.authservice.invitation.domain.Invitation;
import com.learning.authservice.invitation.domain.InvitationStatus;
import com.learning.authservice.invitation.dto.InvitationRequest;
import com.learning.authservice.invitation.dto.InvitationResponse;
import com.learning.authservice.invitation.repository.InvitationRepository;
import com.learning.authservice.service.EmailService;
import com.learning.authservice.signup.CognitoUserRegistrar;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing user invitations.
 * Simplified for lite version - no multi-tenancy.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InvitationServiceImpl implements InvitationService {
    private final InvitationRepository invitationRepository;
    private final EmailService emailService;
    private final CognitoUserRegistrar cognitoUserRegistrar;
    private final CognitoProperties cognitoProperties;
    private final software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient cognitoClient;

    @Value("${app.invitation.expiration-hours:48}")
    private int expirationHours;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Override
    @Transactional
    public InvitationResponse createInvitation(String invitedBy, InvitationRequest request) {
        log.info("Creating invitation for email={} by={}", request.getEmail(), invitedBy);

        // 1. Check for existing pending invitation (use findByEmailAndStatus to avoid
        // duplicate result issues)
        invitationRepository.findByEmailAndStatus(request.getEmail(), InvitationStatus.PENDING).ifPresent(existing -> {
            throw new IllegalStateException("Active invitation already exists for this email");
        });

        // 2. Generate Token
        String token = generateSecureToken();

        // 3. Create Invitation
        Invitation invitation = Invitation.builder().email(request.getEmail()).roleId(request.getRoleId()).token(token)
                .status(InvitationStatus.PENDING).invitedBy(invitedBy)
                .expiresAt(Instant.now().plus(expirationHours, ChronoUnit.HOURS)).build();

        invitation = invitationRepository.save(invitation);

        // 4. Send Email
        String inviteLink = buildInvitationLink(token);
        emailService.sendInvitationEmail(request.getEmail(), inviteLink, "default");

        return mapToResponse(invitation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvitationResponse> getInvitations() {
        return invitationRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void revokeInvitation(UUID invitationId) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalStateException("Cannot revoke invitation with status: " + invitation.getStatus());
        }

        invitation.setStatus(InvitationStatus.REVOKED);
        invitationRepository.save(invitation);
        log.info("Revoked invitation id={}", invitationId);
    }

    @Override
    @Transactional
    public void resendInvitation(UUID invitationId) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalStateException("Cannot resend invitation with status: " + invitation.getStatus());
        }

        if (invitation.getExpiresAt().isBefore(Instant.now())) {
            invitation.setExpiresAt(Instant.now().plus(expirationHours, ChronoUnit.HOURS));
            invitationRepository.save(invitation);
        }

        String inviteLink = buildInvitationLink(invitation.getToken());
        emailService.sendInvitationEmail(invitation.getEmail(), inviteLink, "default");
    }

    /**
     * Builds invitation link with Angular hash routing.
     * Uses /#/ for Angular hash-based routing.
     */
    private String buildInvitationLink(String token) {
        return frontendUrl + "/#/auth/join?token=" + token;
    }

    @Override
    @Transactional(readOnly = true)
    public Invitation validateInvitation(String token) {
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid invitation token"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalStateException("Invitation is no longer valid (Status: " + invitation.getStatus() + ")");
        }

        if (invitation.getExpiresAt().isBefore(Instant.now())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            invitationRepository.save(invitation);
            throw new IllegalStateException("Invitation has expired");
        }

        return invitation;
    }

    @Override
    @Transactional
    public void acceptInvitation(String token, String password, String name) {
        Invitation invitation = validateInvitation(token);
        String tenantId = "default"; // Single
                                     // tenant
                                     // mode

        log.info("Accepting invitation for email={} tenant={} role={}", invitation.getEmail(), tenantId,
                invitation.getRoleId());

        // 1. Create Cognito user (or skip if exists)
        CognitoUserRegistrar.RegistrationResult result = cognitoUserRegistrar.registerIfNotExists(invitation.getEmail(),
                password, name, tenantId, invitation.getRoleId());

        // 2. Auto-confirm the user (invitation link proves email ownership)
        if (result == CognitoUserRegistrar.RegistrationResult.CREATED) {
            autoConfirmUser(invitation.getEmail());
        }

        // 3. Mark invitation as accepted
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitationRepository.save(invitation);

        log.info("Invitation accepted: email={} cognitoResult={}", invitation.getEmail(), result);
    }

    /**
     * Auto-confirm a user in Cognito since invitation link proves email ownership.
     */
    private void autoConfirmUser(String email) {
        try {
            var confirmRequest = software.amazon.awssdk.services.cognitoidentityprovider.model.AdminConfirmSignUpRequest
                    .builder().userPoolId(cognitoProperties.getUserPoolId()).username(email).build();
            cognitoClient.adminConfirmSignUp(confirmRequest);

            // Also verify email attribute
            var updateRequest = software.amazon.awssdk.services.cognitoidentityprovider.model.AdminUpdateUserAttributesRequest
                    .builder().userPoolId(cognitoProperties.getUserPoolId()).username(email)
                    .userAttributes(software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType
                            .builder().name("email_verified").value("true").build())
                    .build();
            cognitoClient.adminUpdateUserAttributes(updateRequest);

            log.info("User auto-confirmed and email verified: {}", email);
        } catch (Exception e) {
            log.error("Failed to auto-confirm user: {} error={}", email, e.getMessage());
            throw new RuntimeException("Failed to confirm user: " + e.getMessage());
        }
    }

    private String getUserSubFromCognito(String email) {
        var getUserRequest = software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserRequest.builder()
                .userPoolId(cognitoProperties.getUserPoolId()).username(email).build();

        var userResponse = cognitoClient.adminGetUser(getUserRequest);
        return userResponse.userAttributes().stream().filter(attr -> "sub".equals(attr.name()))
                .map(software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType::value).findFirst()
                .orElseThrow(() -> new RuntimeException("Failed to get user sub from Cognito"));
    }

    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private InvitationResponse mapToResponse(Invitation invitation) {
        return InvitationResponse.builder().id(invitation.getId()).email(invitation.getEmail())
                .roleId(invitation.getRoleId()).status(invitation.getStatus()).invitedBy(invitation.getInvitedBy())
                .expiresAt(invitation.getExpiresAt()).createdAt(invitation.getCreatedAt()).build();
    }
}
