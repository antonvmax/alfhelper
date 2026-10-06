package ru.alfastrah.site.avto.ws.contact.signed.config.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.lang.NonNull;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class JdbcConversionConfig extends AbstractJdbcConfiguration {

    private final SendContractSignedRequestToJsonConverter sendContractSignedRequestToJsonConverter;
    private final JsonToSendContractSignedRequestConverter jsonToSendContractSignedRequestConverter;

    @Override
    public @NonNull JdbcCustomConversions jdbcCustomConversions() {
        List<Converter<?, ?>> list = List.of(sendContractSignedRequestToJsonConverter, jsonToSendContractSignedRequestConverter);
        return new JdbcCustomConversions(list);
    }
}