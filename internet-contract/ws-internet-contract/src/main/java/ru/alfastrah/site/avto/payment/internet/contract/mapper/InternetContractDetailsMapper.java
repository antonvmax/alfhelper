package ru.alfastrah.site.avto.payment.internet.contract.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.alfastrah.site.avto.model.internet.contract.dto.InternetContractDetails;
import ru.alfastrah.site.avto.payment.internet.contract.entity.InternetContract;
import ru.alfastrah.site.avto.payment.internet.contract.entity.InternetContractEntity;
import ru.alfastrah.site.avto.payment.internet.contract.entity.InternetContractPayment;
import ru.alfastrah.site.avto.payment.internet.contract.entity.InternetContractPaymentEntity;

@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public abstract class InternetContractDetailsMapper {

    public abstract InternetContractDetails from(InternetContract c);

    public abstract InternetContractDetails from(InternetContractPayment p);

    // Postgres-сущности: в internet_contract дата оплаты лежит в is_paid, payment_dict_id — числовой.
    @Mapping(target = "paid", source = "isPaid")
    public abstract InternetContractDetails from(InternetContractEntity c);

    // В строке платежа дата оплаты — date_response.
    @Mapping(target = "paid", source = "dateResponse")
    public abstract InternetContractDetails from(InternetContractPaymentEntity p);
}
