package ru.alfastrah.site.avto.ws.partners.interaction.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class RestInterceptorConfig implements WebMvcConfigurer {

    private final ThreadLocal<String> login;

    public RestInterceptorConfig(ThreadLocal<String> login) {
        this.login = login;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new JwtPrincipalInterceptor(login))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/actuator/**",
                        "/cxf/**");
    }
}