package ru.alfastrah.site.avto.payment.internet.contract.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.alfastrah.site.avto.model.internet.contract.dto.InternetContractPaymentRequest;
import ru.alfastrah.site.avto.model.internet.contract.dto.InternetContractRequest;
import ru.alfastrah.site.avto.payment.internet.contract.repository.ProcedureRepository;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class InternetContractWriteService {

    private final ProcedureRepository procedureRepository;

    private final InternetContractMirrorService mirrorService;

    public void createContract(InternetContractRequest request) {
        procedureRepository.createInternetContract(
                request.contractId(),
                request.internetContractNumber(),
                request.dealerId(),
                request.productId()
        );
        try {
            mirrorService.mirrorContractCreate(request);
        } catch (Exception e) {
            log.error("Postgres mirror: не удалось сохранить internet_contract для contractId={}: {}",
                    request.contractId(), e.getMessage(), e);
        }
    }

    public Long createPayment(InternetContractPaymentRequest request) {
        procedureRepository.pF2AmountMessage(
                BigDecimal.valueOf(request.contractId()),
                request.mdOrder(),
                request.paidAmount(),
                request.paymentDictId()
        );
        try {
            return mirrorService.mirrorPaymentCreate(request);
        } catch (Exception e) {
            log.error("Postgres mirror: не удалось сохранить internet_contract_payment для contractId={}: {}",
                    request.contractId(), e.getMessage(), e);
            return null;
        }
    }
}
