package ru.alfastrah.site.avto.ws.contact.signed.config.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.SendContractSignedRequest;

@Slf4j
@Component
@ReadingConverter
@RequiredArgsConstructor
public class JsonToSendContractSignedRequestConverter implements Converter<PGobject, SendContractSignedRequest> {

    private final ObjectMapper mapper;

    @Override
    public SendContractSignedRequest convert(@NotNull PGobject source) {
        try {
            return mapper.readValue(source.getValue(), SendContractSignedRequest.class);
        } catch (JsonProcessingException e) {
            log.error("Can't convert EmailSendLog to json: {}", source);
            return null;
        }
    }
}