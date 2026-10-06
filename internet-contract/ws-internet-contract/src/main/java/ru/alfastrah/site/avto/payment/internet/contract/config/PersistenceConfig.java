package ru.alfastrah.site.avto.payment.internet.contract.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class PersistenceConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.unicus")
    public DataSourceProperties unicusDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("jdbcTemplate")
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.unicus.hikari")
    public DataSource dataSource() {
        return unicusDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean("unicusJdbcTemplate")
    public NamedParameterJdbcTemplate unicusJdbcTemplate(DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }
}
