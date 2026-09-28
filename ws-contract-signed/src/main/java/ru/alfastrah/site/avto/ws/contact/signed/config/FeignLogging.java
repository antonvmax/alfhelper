package ru.alfastrah.site.avto.ws.contact.signed.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.alfastrah.site.avto.ws.contact.signed.logging.NormalizedFeignLogger;


@Configuration
public class FeignLogging {

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public Logger logger() {
        return new NormalizedFeignLogger();
    }
}
