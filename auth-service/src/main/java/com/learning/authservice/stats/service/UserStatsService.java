package com.learning.authservice.stats.service;

import com.learning.authservice.invitation.domain.InvitationStatus;
import com.learning.authservice.invitation.repository.InvitationRepository;
import com.learning.authservice.stats.dto.UserStatsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Service for calculating user statistics.
 * Tenant isolation is handled via TenantDataSourceRouter.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserStatsService {

        private final InvitationRepository invitationRepository;

        /**
         * Get comprehensive user statistics.
         *
         * @return User statistics DTO
         */
        @Transactional(readOnly = true)
        public UserStatsDTO getUserStats() {
                log.info("Calculating user statistics");

                // Count invitations by status
                long pendingInvitations = invitationRepository.countByStatus(InvitationStatus.PENDING);
                long acceptedInvitations = invitationRepository.countByStatus(InvitationStatus.ACCEPTED);
                long expiredInvitations = invitationRepository.countByStatus(InvitationStatus.EXPIRED);
                long revokedInvitations = invitationRepository.countByStatus(InvitationStatus.REVOKED);

                // Total users = accepted invitations (users who joined)
                long totalUsers = acceptedInvitations;

                // Calculate role distribution - Simplified for lite version (no RBAC)
                Map<String, Long> roleDistribution = Map.of("user", totalUsers);

                // Extract specific role counts
                long adminCount = 0L; // No distinct admin role tracking in DB anymore
                long regularUserCount = totalUsers;

                UserStatsDTO stats = UserStatsDTO.builder()
                                .totalUsers(totalUsers)
                                .pendingInvitations(pendingInvitations)
                                .expiredInvitations(expiredInvitations)
                                .revokedInvitations(revokedInvitations)
                                .roleDistribution(roleDistribution)
                                .adminCount(adminCount)
                                .regularUserCount(regularUserCount)
                                .build();

                log.info("User stats: {} total users, {} pending invitations", totalUsers, pendingInvitations);

                return stats;
        }
}
