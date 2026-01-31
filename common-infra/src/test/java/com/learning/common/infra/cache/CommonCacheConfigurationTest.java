package com.learning.common.infra.cache;

import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.CacheManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for CommonCacheConfiguration.
 * Verifies that all expected caches are properly configured.
 */
class CommonCacheConfigurationTest {

    private final CommonCacheConfiguration config = new CommonCacheConfiguration();

    @Test
    void cacheManagerCreatesPermissionsCache_WithoutRedisson() {
        // Test fallback to Caffeine when Redisson is not available
        @SuppressWarnings("unchecked")
        ObjectProvider<RedissonClient> mockProvider = mock(ObjectProvider.class);
        when(mockProvider.getIfAvailable()).thenReturn(null);

        CacheManager cacheManager = config.cacheManager(mockProvider);

        assertThat(cacheManager.getCache(CacheNames.PERMISSIONS))
                .as("Permissions cache should exist")
                .isNotNull();
    }

    @Test
    void cacheNamesAreCorrectConstants() {
        // Verify the constants match expected values (prevents typos)
        assertThat(CacheNames.PERMISSIONS).isEqualTo("permissions");
    }

    @Test
    void distributedCachesSetContainsExpectedCaches() {
        assertThat(CacheNames.DISTRIBUTED_CACHES)
                .contains(CacheNames.PERMISSIONS)
                .contains(CacheNames.USER_PERMISSIONS)
                .contains(CacheNames.USER_ALL_PERMISSIONS);
    }

    @Test
    void localCachesSetIsEmpty() {
        assertThat(CacheNames.LOCAL_CACHES).isEmpty();
    }

    @Test
    void isDistributedReturnsTrueForDistributedCaches() {
        assertThat(CacheNames.isDistributed(CacheNames.PERMISSIONS)).isTrue();
        assertThat(CacheNames.isDistributed(CacheNames.USER_PERMISSIONS)).isTrue();
    }
}
