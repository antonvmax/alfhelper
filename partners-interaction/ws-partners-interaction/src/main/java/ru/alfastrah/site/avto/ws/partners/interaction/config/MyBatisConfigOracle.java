package ru.alfastrah.site.avto.ws.partners.interaction.config;

import jakarta.annotation.Resource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import javax.sql.DataSource;

@Configuration
public class MyBatisConfigOracle {
    @Resource(name="oracleDataSource")
    private DataSource oracleDataSource;

    @Bean
    @Qualifier("oracleSessionFactory")
    public SqlSessionFactory oracleSessionFactory() throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(oracleDataSource);
        sqlSessionFactoryBean.setMapperLocations(new ClassPathResource("mapper/partnersInteractionMapper.xml"));
        return sqlSessionFactoryBean.getObject();
    }
}
