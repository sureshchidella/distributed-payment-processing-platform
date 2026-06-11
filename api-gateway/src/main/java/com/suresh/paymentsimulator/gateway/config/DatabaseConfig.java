package com.suresh.paymentsimulator.gateway.config;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariDataSource;

/**
 * Database configuration for PostgreSQL with HikariCP connection pooling.
 * Configures JPA/Hibernate for the common-service entities and repositories.
 * Enables transaction management and repository scanning.
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.suresh.paymentsimulator.common.repository")
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    @Value("${spring.datasource.driver-class-name:org.postgresql.Driver}")
    private String driverClassName;

    @Value("${spring.jpa.hibernate.ddl-auto:validate}")
    private String ddlAuto;

    @Value("${spring.jpa.show-sql:false}")
    private boolean showSql;

    @Value("${spring.jpa.properties.hibernate.dialect:org.hibernate.dialect.PostgreSQLDialect}")
    private String dialect;

    /**
     * Creates and configures the HikariCP data source connection pool.
     * Pool settings optimized for moderate concurrency.
     *
     * @return configured HikariDataSource for PostgreSQL
     */
    @Bean
    public DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(datasourceUrl);
        dataSource.setUsername(datasourceUsername);
        dataSource.setPassword(datasourcePassword);
        dataSource.setDriverClassName(driverClassName);
        dataSource.setMaximumPoolSize(20);
        dataSource.setMinimumIdle(5);
        dataSource.setConnectionTimeout(30000);
        dataSource.setIdleTimeout(600000);
        dataSource.setMaxLifetime(1800000);
        return dataSource;
    }

    /**
     * Creates the JPA EntityManagerFactory for entity management.
     * Scans the common-service entity package for JPA entities.
     *
     * @param dataSource the configured data source
     * @return LocalContainerEntityManagerFactoryBean for JPA
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.suresh.paymentsimulator.common.entity");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setShowSql(showSql);
        vendorAdapter.setGenerateDdl(true);
        vendorAdapter.setDatabasePlatform(dialect);
        em.setJpaVendorAdapter(vendorAdapter);

        return em;
    }

    /**
     * Creates the JPA transaction manager for declarative transaction management.
     *
     * @param entityManagerFactory the JPA entity manager factory
     * @return PlatformTransactionManager for Spring transactions
     */
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}