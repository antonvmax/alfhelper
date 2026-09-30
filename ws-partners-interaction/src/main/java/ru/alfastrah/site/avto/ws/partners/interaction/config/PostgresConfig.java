package ru.alfastrah.site.avto.ws.partners.interaction.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
public class PostgresConfig {

    @Bean
    @Qualifier("postgresDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.postgres")
    public DataSource postgresDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @Qualifier("postgresTransactionManager")
    public DataSourceTransactionManager postgresTransactionManager(@Qualifier("postgresDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public SpringLiquibase liquibase(@Qualifier("postgresDataSource") DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:liquibase/liquibase-changelog.yaml");
        liquibase.setDatabaseChangeLogLockTable("partners_interaction_DATABASECHANGELOGLOCK");
        liquibase.setLiquibaseSchema("public");
        return liquibase;
    }
}