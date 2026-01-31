package com.learning.authservice.invitation.service;

import com.learning.authservice.invitation.domain.Invitation;
import com.learning.authservice.invitation.domain.InvitationStatus;
import com.learning.authservice.invitation.dto.InvitationRequest;
import com.learning.authservice.invitation.dto.InvitationResponse;
import com.learning.authservice.invitation.repository.InvitationRepository;
import com.learning.authservice.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import com.learning.authservice.config.CognitoProperties;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvitationServiceTest {

    @Mock
    private InvitationRepository invitationRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private com.learning.authservice.signup.CognitoUserRegistrar cognitoUserRegistrar;
    @Mock
    private CognitoProperties cognitoProperties;
    @Mock
    private CognitoIdentityProviderClient cognitoClient;

    private InvitationServiceImpl invitationService;

    @BeforeEach
    void setUp() {
        invitationService = new InvitationServiceImpl(
                invitationRepository,
                emailService,
                cognitoUserRegistrar,
                cognitoProperties,
                cognitoClient);
        org.springframework.test.util.ReflectionTestUtils.setField(invitationService, "expirationHours", 48);
        org.springframework.test.util.ReflectionTestUtils.setField(invitationService, "frontendUrl",
                "http://localhost:4200");
    }

    @Test
    void createInvitation_Success() {
        // Arrange
        String invitedBy = "admin-user";
        InvitationRequest request = new InvitationRequest("test@example.com", "role-user");

        when(invitationRepository.findByEmailAndStatus(request.getEmail(), InvitationStatus.PENDING))
                .thenReturn(Optional.empty());
        when(invitationRepository.save(any(Invitation.class))).thenAnswer(invocation -> {
            Invitation inv = invocation.getArgument(0);
            inv.setId(UUID.randomUUID());
            inv.setCreatedAt(Instant.now());
            return inv;
        });

        // Act
        InvitationResponse response = invitationService.createInvitation(invitedBy, request);

        // Assert
        assertNotNull(response);
        assertEquals(request.getEmail(), response.getEmail());
        assertEquals(InvitationStatus.PENDING, response.getStatus());

        verify(emailService).sendInvitationEmail(eq(request.getEmail()), anyString(), eq("default"));
    }

    @Test
    void revokeInvitation_Success() {
        // Arrange
        UUID invitationId = UUID.randomUUID();
        Invitation invitation = new Invitation();
        invitation.setId(invitationId);
        invitation.setStatus(InvitationStatus.PENDING);

        when(invitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));

        // Act
        invitationService.revokeInvitation(invitationId);

        // Assert
        assertEquals(InvitationStatus.REVOKED, invitation.getStatus());
        verify(invitationRepository).save(invitation);
    }
}
