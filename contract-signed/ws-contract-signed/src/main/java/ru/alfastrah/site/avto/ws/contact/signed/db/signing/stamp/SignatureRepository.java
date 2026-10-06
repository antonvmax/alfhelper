package ru.alfastrah.site.avto.ws.contact.signed.db.signing.stamp;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import ru.alfastrah.site.avto.ws.contact.signed.db.model.StampParams;
import ru.alfastrah.site.avto.ws.contact.signed.service.Product;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SignatureRepository {
    private final SqlSessionFactory postgreSqlSessionFactory;

    public SignatureRepository(SqlSessionFactory postgreSqlSessionFactory) {
        this.postgreSqlSessionFactory = postgreSqlSessionFactory;
    }

    public List<StampParams> getStampParams(Product product, String formId) {
        try (SqlSession sqlSession = postgreSqlSessionFactory.openSession()) {
            final SignatureMapper mapper = sqlSession.getMapper(SignatureMapper.class);
            return mapper.getStampParams(product.getProductId(), formId);
        } catch (Exception e) {
            log.error("Ошибка получения информации о штапме подписи из бд. Product {}, formId {}",
                    product.getProductId(), formId, e);
        }
        return new ArrayList<>();
    }

    public String getConfigValue(String paramName) {
        try (SqlSession sqlSession = postgreSqlSessionFactory.openSession()) {
            final SignatureMapper mapper = sqlSession.getMapper(SignatureMapper.class);
            return mapper.getSigningParam(paramName);
        } catch (Exception e) {
            log.error("Ошибка получения параметра конфигурации. ParamName = {}",
                    paramName, e);
        }
        return null;
    }

    public void logNotSignedPolicy(BigInteger contractId) {
        try (SqlSession sqlSession = postgreSqlSessionFactory.openSession()) {
            final SignatureMapper mapper = sqlSession.getMapper(SignatureMapper.class);
            mapper.insertData(contractId);
        } catch (Exception e) {
            log.error("Ошибка логирования неподписанного договора. ContractId = {}",
                    contractId, e);
        }
    }
}
