package com.learning.authservice.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Test-specific datasource configuration.
 * Provides a default datasource pointing to the Testcontainers DB.
 */
@TestConfiguration
public class TestDataSourceConfig {

    /**
     * Provide a default datasource.
     * Uses the static Testcontainers PostgreSQL instance from
     * AbstractIntegrationTest.
     */
    @Bean(name = "tenantDataSource")
    @Primary
    public DataSource tenantDataSource() {
        // Get reference to static Testcontainers instance
        var postgres = AbstractIntegrationTest.postgres;

        // Create a default datasource for test (points to the Testcontainers DB)
        HikariDataSource defaultDataSource = new HikariDataSource();
        defaultDataSource.setJdbcUrl(postgres.getJdbcUrl());
        defaultDataSource.setUsername(postgres.getUsername());
        defaultDataSource.setPassword(postgres.getPassword());
        defaultDataSource.setMaximumPoolSize(5);
        defaultDataSource.setPoolName("test-default-pool");

        return defaultDataSource;
    }
}
