package ru.alfastrah.site.avto.ws.partners.interaction.logging;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignLogging {

    @Bean
    public NormalizedFeignLogger logger() {
        return new NormalizedFeignLogger();
    }

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}
