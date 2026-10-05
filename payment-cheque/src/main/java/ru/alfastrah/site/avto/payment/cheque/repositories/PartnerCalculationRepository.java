package ru.alfastrah.site.avto.payment.cheque.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class PartnerCalculationRepository {
    private final NamedParameterJdbcTemplate unicusJdbcTemplate;

    public boolean isAssociatedUpidWithContract(String upid, String contractId) {
        Map<String, Object> param = new HashMap<>();
        param.put("contractId", contractId);
        param.put("upid", upid);

        List<Boolean> result =  unicusJdbcTemplate.query("select nvl(max(1), 0) FROM INTERPLAT.PARTNER_CALCULATION PC left " +
                        "join STAFF.ADDITIONAL_PROPOSAL AP on AP.CONTRACT_ID = PC.CONTRACT_ID and AP.ACCIDENT_CONTRACT_ID = :contractId " +
                        " WHERE PC.UPID = :upid" +
                        " and (AP.ADDITIONAL_PROPOSAL_ID is not null or PC.CONTRACT_ID =:contractId)",
                param, (resultSet, rowNumber) -> resultSet.getLong(1) == 1);

        if (CollectionUtils.isEmpty(result)) {
            return false;
        }

        return result.get(0);
    }
}
