package ru.alfastrah.site.avto.ws.contact.signed.db.signing.stamp;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.alfastrah.site.avto.ws.contact.signed.db.model.StampParams;
import ru.alfastrah.site.avto.ws.contact.signed.service.Product;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignatureRepositoryTest {

    @Mock
    private SqlSessionFactory sqlSessionFactory;

    @Mock
    private SqlSession sqlSession;

    @Mock
    private SignatureMapper signatureMapper;

    private SignatureRepository signatureRepository;

    @BeforeEach
    void setUp() {
        signatureRepository = new SignatureRepository(sqlSessionFactory);
    }

    @Test
    void getStampParams_shouldReturnListFromMapper() {
        Product product = Product.OSAGO;
        String formId = "FORM_1";
        List<StampParams> expectedParams = List.of(createStampParams());

        when(sqlSessionFactory.openSession()).thenReturn(sqlSession);
        when(sqlSession.getMapper(SignatureMapper.class)).thenReturn(signatureMapper);
        when(signatureMapper.getStampParams(product.getProductId(), formId)).thenReturn(expectedParams);

        List<StampParams> result = signatureRepository.getStampParams(product, formId);

        assertEquals(expectedParams, result);
        verify(sqlSession).close();
    }

    @Test
    void getStampParams_shouldReturnEmptyListOnException() {
        Product product = Product.OSAGO;
        String formId = "FORM_1";

        when(sqlSessionFactory.openSession()).thenReturn(sqlSession);
        when(sqlSession.getMapper(SignatureMapper.class)).thenReturn(signatureMapper);
        when(signatureMapper.getStampParams(product.getProductId(), formId)).thenThrow(new RuntimeException("DB error"));

        List<StampParams> result = signatureRepository.getStampParams(product, formId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(sqlSession).close();
    }

    @Test
    void getConfigValue_shouldReturnValueFromMapper() {
        String paramName = "some.param";
        String expectedValue = "value";

        when(sqlSessionFactory.openSession()).thenReturn(sqlSession);
        when(sqlSession.getMapper(SignatureMapper.class)).thenReturn(signatureMapper);
        when(signatureMapper.getSigningParam(paramName)).thenReturn(expectedValue);

        String result = signatureRepository.getConfigValue(paramName);

        assertEquals(expectedValue, result);
        verify(sqlSession).close();
    }

    @Test
    void getConfigValue_shouldReturnNullOnException() {
        String paramName = "some.param";

        when(sqlSessionFactory.openSession()).thenReturn(sqlSession);
        when(sqlSession.getMapper(SignatureMapper.class)).thenReturn(signatureMapper);
        when(signatureMapper.getSigningParam(paramName)).thenThrow(new RuntimeException("DB error"));

        String result = signatureRepository.getConfigValue(paramName);

        assertNull(result);
        verify(sqlSession).close();
    }

    @Test
    void logNotSignedPolicy_shouldCallMapperInsertData() {
        BigInteger contractId = BigInteger.valueOf(12345L);

        when(sqlSessionFactory.openSession()).thenReturn(sqlSession);
        when(sqlSession.getMapper(SignatureMapper.class)).thenReturn(signatureMapper);

        signatureRepository.logNotSignedPolicy(contractId);

        verify(signatureMapper).insertData(contractId);
        verify(sqlSession).close();
    }

    @Test
    void logNotSignedPolicy_shouldCatchExceptionAndLog() {
        BigInteger contractId = BigInteger.valueOf(12345L);

        when(sqlSessionFactory.openSession()).thenReturn(sqlSession);
        when(sqlSession.getMapper(SignatureMapper.class)).thenReturn(signatureMapper);
        doThrow(new RuntimeException("DB error")).when(signatureMapper).insertData(contractId);

        signatureRepository.logNotSignedPolicy(contractId);

        verify(sqlSession).close();
    }

    private StampParams createStampParams() {
        StampParams params = new StampParams();
        params.setLlx(10.0f);
        params.setLly(20.0f);
        params.setUrx(30.0f);
        params.setUry(40.0f);
        params.setFontSize(12);
        params.setPage(1);
        params.setStampVisible(true);
        return params;
    }
}