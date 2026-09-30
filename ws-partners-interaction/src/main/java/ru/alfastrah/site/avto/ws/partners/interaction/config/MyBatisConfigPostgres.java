package ru.alfastrah.site.avto.ws.partners.interaction.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.UUID;

@Configuration
@MapperScan(basePackages = "ru.alfastrah.site.avto.ws.partners.interaction.mapper",
        sqlSessionFactoryRef = "postgresSessionFactory")
public class MyBatisConfigPostgres {

    @Bean
    @Qualifier("postgresSessionFactory")
    public SqlSessionFactory postgresSessionFactory(@Qualifier("postgresDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);

        // Регистрируем TypeHandler до создания SqlSessionFactory
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        typeHandlerRegistry.register(UUID.class, new UuidTypeHandler());

        sqlSessionFactoryBean.setConfiguration(configuration);

        return sqlSessionFactoryBean.getObject();
    }
}