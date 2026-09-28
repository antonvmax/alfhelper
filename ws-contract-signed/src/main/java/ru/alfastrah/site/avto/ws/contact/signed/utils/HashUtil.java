package ru.alfastrah.site.avto.ws.contact.signed.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.SendContractSignedRequest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
@RequiredArgsConstructor
public class HashUtil {

    private final ObjectMapper objectMapper;

    public String getHashCode(SendContractSignedRequest request) throws JsonProcessingException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String inputData = serializeToString(request);
        byte[] hashBytes = digest.digest(inputData.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hashBytes);
    }

    private String serializeToString(SendContractSignedRequest request) throws JsonProcessingException {
        return objectMapper.writeValueAsString(request);
    }
}
