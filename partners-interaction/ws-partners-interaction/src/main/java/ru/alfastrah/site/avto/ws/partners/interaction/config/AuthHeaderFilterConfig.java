package ru.alfastrah.site.avto.ws.partners.interaction.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Регистрация {@link AuthHeaderFilter} для REST-канала.
 *
 * <p>Фильтр применяется только к REST-запросам (исключая SOAP на {@code /cxf/**}),
 * чтобы не вмешиваться в аутентификацию, выполняемую CXF/ws-security.</p>
 */
@Configuration
public class AuthHeaderFilterConfig {

    @Bean
    public FilterRegistrationBean<AuthHeaderFilter> authFilter() {
        FilterRegistrationBean<AuthHeaderFilter> registration = new FilterRegistrationBean<>(new AuthHeaderFilter());
        registration.addUrlPatterns("/*");
        registration.setName("authHeaderFilter");
        registration.setOrder(1);
        return registration;
    }
}