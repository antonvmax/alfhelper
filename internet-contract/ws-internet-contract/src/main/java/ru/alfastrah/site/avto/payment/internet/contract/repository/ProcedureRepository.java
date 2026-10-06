package ru.alfastrah.site.avto.payment.internet.contract.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import ru.alfastrah.site.avto.payment.internet.contract.entity.PF2AmountMessage;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ProcedureRepository {

    @Qualifier("pF2AmountMessageJdbcCall")
    private final SimpleJdbcCall pF2AmountMessageJdbcCall;

    @Qualifier("pF2M1MessageJdbcCall")
    private final SimpleJdbcCall pF2M1MessageJdbcCall;

    @Qualifier("f2m1MessageWithoutEmailJdbcCall")
    private final SimpleJdbcCall f2m1MessageWithoutEmailJdbcCall;

    @Qualifier("setPaymentDictJdbcCall")
    private final SimpleJdbcCall setPaymentDictJdbcCall;

    @Qualifier("updateStatusLastTransactJdbcCall")
    private final SimpleJdbcCall updateStatusLastTransactJdbcCall;

    @Qualifier("setContractStatusJdbcCall")
    private final SimpleJdbcCall setContractStatusJdbcCall;

    @Qualifier("fixInternetSaleJdbcCall")
    private final SimpleJdbcCall fixInternetSaleJdbcCall;

    public String pF2AmountMessage(PF2AmountMessage entity) {
        return pF2AmountMessage(entity.getContractId(), entity.getMdOrder(), entity.getAmount(), entity.getPaymentDictId());
    }

    public String pF2AmountMessage(BigDecimal contractId, String mdOrder, BigDecimal amount, Long paymentDictId) {
        Map<String, Object> param = new HashMap<>();
        param.put("p_contract_id", contractId);
        param.put("p_order", mdOrder);
        param.put("p_amount", amount);
        param.put("p_payment_dict_id", paymentDictId);

        Map<String, Object> result = pF2AmountMessageJdbcCall.execute(param);
        String res = (String) result.get("p_error");
        return StringUtils.isEmpty(res) ? "success" : res;
    }

    public String pF2M1Message(BigDecimal contractId, String mdOrder, String xml) {
        Map<String, Object> param = new HashMap<>();
        param.put("p_contract_id", contractId);
        param.put("p_order", mdOrder);
        param.put("p_xml", xml);
        Map<String, Object> result = pF2M1MessageJdbcCall.execute(param);
        log.info("staff.inet_card_pak.p_f2m1_message procedure call response: {}", result);
        return "true".equals(result.get("p_success")) ? "success" : (String) result.get("p_message");
    }

    public String f2m1MessageWithoutEmail(BigDecimal contractId, String mdOrder, String xml) {
        Map<String, Object> param = new HashMap<>();
        param.put("p_contract_id", contractId);
        param.put("p_order", mdOrder);
        param.put("p_xml", xml);
        Map<String, Object> result = f2m1MessageWithoutEmailJdbcCall.execute(param);
        log.info("staff.inet_card_pak.p_f2m1_message_without_email procedure call response: {}", result);
        return "true".equals(result.get("p_success")) ? "success" : (String) result.get("p_message");
    }

    public void setPaymentDict(Long contractId, String mdOrder, Long paymentDictId) {
        Map<String, Object> param = new HashMap<>();
        param.put("p_contract_id", contractId != null ? BigDecimal.valueOf(contractId) : null);
        param.put("p_order", mdOrder);
        param.put("p_payment_id", paymentDictId != null ? BigDecimal.valueOf(paymentDictId) : null);
        setPaymentDictJdbcCall.execute(param);
    }

    public void updateStatusLastTransact(Long contractId, Integer statusId) {
        Map<String, Object> param = new HashMap<>();
        param.put("p_contract_id", contractId != null ? BigDecimal.valueOf(contractId) : null);
        param.put("p_status_id", statusId);
        updateStatusLastTransactJdbcCall.execute(param);
    }

    public void setContractStatus(Long contractId, Integer newStatusTypeId) {
        Map<String, Object> param = new HashMap<>();
        param.put("p_contract_id", contractId != null ? BigDecimal.valueOf(contractId) : null);
        param.put("p_new_stype_id", newStatusTypeId);
        setContractStatusJdbcCall.execute(param);
    }

    public void createInternetContract(Long contractId, String contractNumber, String dealerId, String productId) {
        Map<String, Object> p = new HashMap<>();
        p.put("p_contract_id", BigDecimal.valueOf(contractId));
        p.put("p_contract_number", contractNumber);
        p.put("p_dealer_id", dealerId);
        p.put("p_product_id", productId);
        fixInternetSaleJdbcCall.execute(p);
    }
}
