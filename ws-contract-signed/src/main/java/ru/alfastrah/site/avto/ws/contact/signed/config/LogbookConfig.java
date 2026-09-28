package ru.alfastrah.site.avto.ws.contact.signed.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.BodyFilter;
import org.zalando.logbook.Logbook;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.zalando.logbook.core.Conditions.exclude;
import static org.zalando.logbook.core.Conditions.requestTo;

@Configuration
public class LogbookConfig {

    @Bean
    public Logbook logbook(ObjectMapper objectMapper) {
        return Logbook.builder()
                .condition(exclude(requestTo("/actuator/**")))
                .bodyFilter(createJacksonFilter(objectMapper))
                .build();
    }

    private BodyFilter createJacksonFilter(ObjectMapper objectMapper) {
        return (contentType, body) -> {
            if (contentType == null || !contentType.contains("application/json")) {
                return new String(body.getBytes(), StandardCharsets.UTF_8);
            }

            try {
                JsonNode jsonNode = objectMapper.readTree(body);
                removeFieldRecursively(jsonNode, "data");
                removeFieldRecursively(jsonNode, "Content");

                return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
            } catch (IOException e) {
                return new String(body.getBytes(), StandardCharsets.UTF_8);
            }
        };
    }

    private void removeFieldRecursively(JsonNode node, String fieldName) {
        if (node.isObject()) {
            ((ObjectNode) node).remove(fieldName);
            for (JsonNode child : node) {
                removeFieldRecursively(child, fieldName);
            }
        } else if (node.isArray()) {
            for (JsonNode item : node) {
                removeFieldRecursively(item, fieldName);
            }
        }
    }
}
