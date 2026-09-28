package ru.alfastrah.site;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import ru.alfastrah.site.avto.adapter.contract.signed.config.SoapAdapterConfiguration;
import ru.alfastrah.site.avto.client.unicus.db.partner.ContractInfoClient;
import ru.alfastrah.site.avto.client.unicus.db.samplePak.SamplePakClient;
import ru.alfastrah.site.avto.ws.contact.signed.client.orange.MsOrangeClient;
import ru.alfastrah.site.avto.ws.contact.signed.service.client.partners.interaction.PartnersInteractionFeignClient;

@SpringBootApplication
@Import(SoapAdapterConfiguration.class)
@EnableFeignClients(clients = {
        PartnersInteractionFeignClient.class,
        ContractInfoClient.class,
        SamplePakClient.class,
        MsOrangeClient.class
})
public class ContractSignedApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContractSignedApplication.class);
    }


    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }

    @Bean
    public MappingJackson2HttpMessageConverter customMappingJackson2HttpMessageConverter(ObjectMapper objectMapper) {
        objectMapper.registerModule(new JakartaXmlBindAnnotationModule());
        return new MappingJackson2HttpMessageConverter(objectMapper);
    }
}
