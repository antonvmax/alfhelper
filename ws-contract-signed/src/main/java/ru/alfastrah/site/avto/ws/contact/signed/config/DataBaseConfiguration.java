package ru.alfastrah.site.avto.ws.contact.signed.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import ru.alfastrah.site.avto.ws.contact.signed.db.signing.stamp.SignatureMapper;

import javax.sql.DataSource;


@Configuration
@EnableJdbcRepositories(
        basePackages = "ru.alfastrah.site.avto.ws.contact.signed.db.repository",
        transactionManagerRef = "postgresTransactionManager")
public class DataBaseConfiguration {


    @Bean
    public SqlSessionFactory postgreSqlSessionFactory(@Qualifier("postgreDataSource") DataSource postgreDataSource) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(postgreDataSource);
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        configuration.addMapper(SignatureMapper.class);
        sqlSessionFactoryBean.setConfiguration(configuration);
        return sqlSessionFactoryBean.getObject();
    }

    @Bean
    public NamedParameterJdbcTemplate postgresJdbcTemplate(DataSource postgreDataSource) {
        return new NamedParameterJdbcTemplate(postgreDataSource);
    }

    @Bean("postgresTransactionManager")
    public PlatformTransactionManager postgresTransactionManager(@Qualifier("postgreDataSource") DataSource postgreDataSource) {
        return new DataSourceTransactionManager(postgreDataSource);
    }

    @Bean
    @ConfigurationProperties("postgresql.datasource")
    public DataSource postgreDataSource() {
        return DataSourceBuilder.create().build();
    }
}
