package ru.alfastrah.site.avto.ws.contact.signed.config;

import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.zalando.logbook.spring.LogbookClientHttpRequestInterceptor;

@Configuration
public class RestConfig {

    @Bean
    public RestTemplate restTemplate(@Qualifier("customMappingJackson2HttpMessageConverter")
                                             HttpMessageConverter<Object> httpMessageConverter,
                                     LogbookClientHttpRequestInterceptor logbookClientHttpRequestInterceptor) {


        BufferingClientHttpRequestFactory factory =
                new BufferingClientHttpRequestFactory(
                        new HttpComponentsClientHttpRequestFactory(
                                HttpClientBuilder.create().build())
                );
        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.getInterceptors().add(logbookClientHttpRequestInterceptor);
        restTemplate.getMessageConverters().add(0, httpMessageConverter);
        return restTemplate;
    }

}
