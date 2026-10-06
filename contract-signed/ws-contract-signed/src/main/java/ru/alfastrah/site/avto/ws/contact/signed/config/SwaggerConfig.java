package ru.alfastrah.site.avto.ws.contact.signed.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Value("${springdoc.swagger-ui.prefix}")
    private String url;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().addServersItem(new Server()
                        .url(url))
                .info(new Info().title("contract-signed")
                        .description("Сервис для получения и отправки на почту подписанных договоров")
                        .contact(new Contact().name("ADT-1")));

    }
}