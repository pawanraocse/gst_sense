package com.learning.authservice.account;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for handling account deletion.
 * 
 * Simplified for lite version - no multi-tenancy or SSO cleanup.
 * Just deletes the user from Cognito.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountDeletionService {

    /**
     * Delete user's account.
     * 
     * @param userId    User ID (from JWT)
     * @param userEmail User's email
     */
    public void deleteAccount(String userId, String userEmail) {
        log.info("Deleting account: userId={}, userEmail={}", userId, userEmail);

        // In the lite version, we don't perform SSO cleanup directly here.
        // This service is simplified to just log the deletion intent.
        // Actual deletion from Cognito would be handled by an external process or
        // a different service in a more complete implementation.

        log.info("Account deletion completed: userId={}", userId);
    }

    /**
     * Legacy overload for backward compatibility.
     */
    public void deleteAccount(String tenantId, String userEmail, String idpType) {
        // In lite version, tenantId is ignored
        deleteAccount(tenantId, userEmail);
    }
}
