package ru.alfastrah.site.avto.payment.internet.contract.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import ru.alfastrah.site.avto.model.internet.contract.dto.InternetContractPaymentRequest;
import ru.alfastrah.site.avto.model.internet.contract.dto.InternetContractRequest;
import ru.alfastrah.site.avto.payment.internet.contract.entity.InternetContractEntity;
import ru.alfastrah.site.avto.payment.internet.contract.entity.InternetContractPaymentEntity;

import java.time.LocalDateTime;

@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public abstract class InternetContractEntityMapper {

    @Mapping(target = "internetContractId", ignore = true)
    public abstract InternetContractEntity toEntity(InternetContractRequest request);

    @Mapping(target = "internetContractPaymentId", ignore = true)
    @Mapping(target = "internetContractId", source = "internetContractId")
    @Mapping(target = "dateRequest", source = "dateRequest")
    public abstract InternetContractPaymentEntity toEntity(
            InternetContractPaymentRequest request,
            Long internetContractId,
            LocalDateTime dateRequest);
}
