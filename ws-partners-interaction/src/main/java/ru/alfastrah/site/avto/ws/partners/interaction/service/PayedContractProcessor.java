package ru.alfastrah.site.avto.ws.partners.interaction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.alfastrah.interplat4.partners.interaction.PayedContractRequest;
import ru.alfastrah.interplat4.partners.interaction.PayedContractResponse;
import ru.alfastrah.site.avto.ws.partners.interaction.dlo.PartnersPakDLOImpl;
import ru.alfastrah.site.avto.ws.partners.interaction.dto.ContractResult;
import ru.alfastrah.site.avto.ws.partners.interaction.parameters.PartnerContract;

import java.math.BigInteger;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayedContractProcessor {

    private final PartnersPakDLOImpl partnersPakDLO;

    private final PartnersPostgresService partnersPostgresService;

    public PayedContractResponse getContract(PayedContractRequest request) {
        // Основной Oracle вызов
        log.trace("getContract перед вызовом partnersPakDLO.getContractId " + request);
        PartnerContract contract = partnersPakDLO.getContractId(request.getUPID());
        log.trace("getContract после вызова partnersPakDLO.getContractId {}", contract);

        // вызов PostgreSQL для логирования и проверки работы логики
        ContractResult postgresResult = null;
        try {
            UUID upid = UUID.fromString(request.getUPID());
            postgresResult = partnersPostgresService.getContractId(upid);

            log.info("PostgreSQL результат для UPID {}: contractId={}, code={}, message='{}'",
                    upid, postgresResult.getContractId(), postgresResult.getCode(), postgresResult.getMessage());

        } catch (Exception e) {
            log.error("PostgreSQL getContractId ошибка для UPID {}: {}", request.getUPID(), e.getMessage());
        }


        PayedContractResponse response = new PayedContractResponse();
        response.setContractId(contract.getContractId());
        response.setCode(contract.getCode());
        response.setMessage(contract.getMessage());

        //todo вернуть при переходе в postgres. Сейчас возвращается значение из Oracle
//        if (postgresResult.getContractId() != null) {
//            response.setContractId(BigInteger.valueOf(postgresResult.getContractId()));
//        }
//        if (postgresResult.getCode() != null) {
//            response.setCode(BigInteger.valueOf(postgresResult.getCode()));
//        }
//        response.setMessage(postgresResult.getMessage());

        return response;
    }
}
