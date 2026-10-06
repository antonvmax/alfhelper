package ru.alfastrah.site.avto.ws.partners.interaction.dlo;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionException;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import ru.alfastrah.site.avto.ws.partners.interaction.parameters.PartnerContract;
import ru.alfastrah.site.avto.ws.partners.interaction.parameters.SaveUPIDParameters;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Repository
public class PartnersPakDLOImpl implements PartnersPakDLO{

    @Autowired
    @Qualifier("oracleSessionFactory")
    private SqlSessionFactory sessionFactory;

    @Override
    public void saveUPID(String UPID, String callerCode) {
        SaveUPIDParameters parameters = new SaveUPIDParameters(UPID, callerCode);
        SqlSession sqlSession = sessionFactory.openSession();
        try (sqlSession) {
            sqlSession.getMapper(PartnersMapper.class).saveUPID(parameters);
        } catch (SqlSessionException e) {
            log.error("Бд не доступна, ошибка: {}", e.getMessage());
        }
    }

    @Override
    public PartnerContract getContractId(String UPID) {
        Map<String, Object> params = new HashMap<>();
        params.put("p_upid", UPID);

        SqlSession sqlSession = sessionFactory.openSession();
        try (sqlSession) {
            sqlSession.getMapper(PartnersMapper.class).getContractId(params);
        } catch (SqlSessionException e) {
            log.error("Бд не доступна, ошибка: {}", e.getMessage());
        }

        PartnerContract contract = new PartnerContract();
        contract.setContractId((BigInteger) params.get("p_contract_id"));
        contract.setCode((BigInteger) params.get("p_code"));
        contract.setMessage((String) params.get("p_message"));

        return contract;
    }

    @Override
    public void linkActionToUpid(String upid, String calcId, Long contractId) {
        Map<String, Object> params = new HashMap<>();
        params.put("p_upid", upid);
        params.put("p_calc_id", calcId);
        params.put("p_contract_id", contractId);
        try (SqlSession session = sessionFactory.openSession()) {
            session.getMapper(PartnersMapper.class).linkActionToUpid(params);
        } catch (SqlSessionException e) {
            log.error("Бд не доступна, ошибка: {}", e.getMessage());
        }
    }

    @Override
    public Long searchByUpidAndContractId(String upid, Long contractId) {
        Map<String, Object> params = new HashMap<>();
        params.put("p_upid", upid);
        params.put("p_contract_id", contractId);
        try (SqlSession session = sessionFactory.openSession()) {
            return session.getMapper(PartnersMapper.class).searchByUpidAndContractId(params);
        } catch (SqlSessionException e) {
            log.error("Бд не доступна, ошибка: {}", e.getMessage());
            throw e;
        }
    }
}
