package ru.alfastrah.site.avto.payment.cheque.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.zalando.logbook.spring.LogbookClientHttpRequestInterceptor;

@Configuration
public class RestConfig {
    @Bean
    public RestTemplate restTemplate(LogbookClientHttpRequestInterceptor logbookClientHttpRequestInterceptor, @Qualifier("customObjectMapper") ObjectMapper objectMapper) {

        BufferingClientHttpRequestFactory factory =
                new BufferingClientHttpRequestFactory(
                        new HttpComponentsClientHttpRequestFactory(HttpClientBuilder.create().build()));

        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.getInterceptors().add(logbookClientHttpRequestInterceptor);
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter(objectMapper));
        return restTemplate;
    }
}

