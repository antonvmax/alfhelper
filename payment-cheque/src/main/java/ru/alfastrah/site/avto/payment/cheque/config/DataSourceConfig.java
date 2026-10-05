package ru.alfastrah.site.avto.payment.cheque.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {
    @Bean
    @ConfigurationProperties("spring.datasource.unicus")
    public DataSourceProperties unicusDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.unicus.hikari")
    public DataSource dataSource() {
        return unicusDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean("unicusJdbcTemplate")
    JdbcTemplate unicusJdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean("namedParameterJdbcTemplate")
    NamedParameterJdbcTemplate namedParameterJdbcTemplate(@Qualifier("unicusJdbcTemplate") JdbcTemplate jdbcTemplate) {
        return new NamedParameterJdbcTemplate(jdbcTemplate);
    }
}
