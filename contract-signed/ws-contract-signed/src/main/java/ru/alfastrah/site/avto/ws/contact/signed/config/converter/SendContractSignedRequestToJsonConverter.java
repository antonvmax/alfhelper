package ru.alfastrah.site.avto.ws.contact.signed.config.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.SendContractSignedRequest;

import java.sql.SQLException;

@Slf4j
@Component
@WritingConverter
@RequiredArgsConstructor
public class SendContractSignedRequestToJsonConverter implements Converter<SendContractSignedRequest, PGobject> {

    private final ObjectMapper mapper;

    @Override
    public PGobject convert(@NotNull SendContractSignedRequest source) {
        try {
            PGobject pGobject = new PGobject();
            pGobject.setType("json");
            pGobject.setValue(mapper.writeValueAsString(source));
            return pGobject;
        } catch (SQLException | JsonProcessingException e) {
            log.error("Can't convert EmailSendLog to json: {}", source);
            return null;
        }
    }
}