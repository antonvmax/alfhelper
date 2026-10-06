package ru.alfastrah.site.avto.ws.partners.interaction.dlo;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionException;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.alfastrah.site.avto.ws.partners.interaction.parameters.SaveUPIDParameters;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartnersPakDLOImplTest {
    @Mock
    private SqlSessionFactory sessionFactory;

    @Mock
    private SqlSession sqlSession;

    @Mock
    private PartnersMapper partnersMapper;

    @InjectMocks
    private PartnersPakDLOImpl service;

    @Test
    void saveUPID_shouldCallMapperWithCorrectParameters() {
        String upid = "UPID_123";
        String callerCode = "CALLER_456";

        when(sessionFactory.openSession()).thenReturn(sqlSession);
        when(sqlSession.getMapper(PartnersMapper.class)).thenReturn(partnersMapper);

        service.saveUPID(upid, callerCode);

        verify(sessionFactory).openSession();
        verify(sqlSession).getMapper(PartnersMapper.class);
        verify(sqlSession).close();

        ArgumentCaptor<SaveUPIDParameters> captor = ArgumentCaptor.forClass(SaveUPIDParameters.class);
        verify(partnersMapper).saveUPID(captor.capture());

        SaveUPIDParameters params = captor.getValue();
        assertEquals(upid, params.getUPID());
        assertEquals(callerCode, params.getCallerCode());
    }

    @Test
    void saveUPID_shouldCatchExceptionAndNotRethrow() {
        String upid = "UPID_123";
        String callerCode = "CALLER_456";

        when(sessionFactory.openSession()).thenReturn(sqlSession);
        when(sqlSession.getMapper(PartnersMapper.class)).thenReturn(partnersMapper);

        doThrow(new SqlSessionException("Бд не доступна")).when(partnersMapper).saveUPID(any());

        assertDoesNotThrow(() -> service.saveUPID(upid, callerCode));

        verify(sessionFactory).openSession();
        verify(sqlSession).close();
    }

    @Test
    void getContractId_shouldCallMapperWithCorrectParameters() {
        String upid = "UPID";

        when(sessionFactory.openSession()).thenReturn(sqlSession);
        when(sqlSession.getMapper(PartnersMapper.class)).thenReturn(partnersMapper);

        service.getContractId(upid);

        verify(sessionFactory).openSession();
        verify(sqlSession).getMapper(PartnersMapper.class);
        verify(sqlSession).close();

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(partnersMapper).getContractId(captor.capture());

        Map<String, Object> param = captor.getValue();
        assertEquals(upid, param.get("p_upid"));
    }

    @Test
    void getContractId_shouldCatchExceptionAndNotRethrow() {
        String upid = "UPID";

        when(sessionFactory.openSession()).thenReturn(sqlSession);
        when(sqlSession.getMapper(PartnersMapper.class)).thenReturn(partnersMapper);

        doThrow(new SqlSessionException("Бд не доступна")).when(partnersMapper).getContractId(any());

        assertDoesNotThrow(() -> service.getContractId(upid));

        verify(sessionFactory).openSession();
        verify(sqlSession).getMapper(PartnersMapper.class);
        verify(sqlSession).close();

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(partnersMapper).getContractId(captor.capture());

        Map<String, Object> param = captor.getValue();
        assertEquals(upid, param.get("p_upid"));
    }

    @Test
    void linkActionToUpid_shouldCallMapperWithCorrectParameters() {
        String upid = "UPID";
        String calcId = "CALC_ID";
        Long contractId = 12345L;

        when(sessionFactory.openSession()).thenReturn(sqlSession);
        when(sqlSession.getMapper(PartnersMapper.class)).thenReturn(partnersMapper);

        service.linkActionToUpid(upid, calcId, contractId);

        verify(sessionFactory).openSession();
        verify(sqlSession).getMapper(PartnersMapper.class);
        verify(sqlSession).close();

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(partnersMapper).linkActionToUpid(captor.capture());

        Map<String, Object> param = captor.getValue();
        assertEquals(upid, param.get("p_upid"));
        assertEquals(calcId, param.get("p_calc_id"));
        assertEquals(contractId, param.get("p_contract_id"));
    }

    @Test
    void linkActionToUpid_shouldCatchExceptionAndNotRethrow() {
        String upid = "UPID";
        String calcId = "CALC_ID";
        Long contractId = 12345L;

        when(sessionFactory.openSession()).thenReturn(sqlSession);
        when(sqlSession.getMapper(PartnersMapper.class)).thenReturn(partnersMapper);
        doThrow(new SqlSessionException("Бд не доступна")).when(partnersMapper).linkActionToUpid(any());

        assertDoesNotThrow(() -> service.linkActionToUpid(upid, calcId, contractId));

        verify(sessionFactory).openSession();
        verify(sqlSession).getMapper(PartnersMapper.class);
        verify(sqlSession).close();
    }

    @Test
    void searchByUpidAndContractId_shouldCallMapperWithCorrectParameters() {
        String upid = "UPID";
        Long contractId = 12345L;

        when(sessionFactory.openSession()).thenReturn(sqlSession);
        when(sqlSession.getMapper(PartnersMapper.class)).thenReturn(partnersMapper);

        service.searchByUpidAndContractId(upid, contractId);

        verify(sessionFactory).openSession();
        verify(sqlSession).getMapper(PartnersMapper.class);
        verify(sqlSession).close();

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(partnersMapper).searchByUpidAndContractId(captor.capture());

        Map<String, Object> param = captor.getValue();
        assertEquals(upid, param.get("p_upid"));
        assertEquals(contractId, param.get("p_contract_id"));
    }

    @Test
    void searchByUpidAndContractId_shouldCatchExceptionAndDoRethrow() {
        String upid = "UPID";
        Long contractId = 12345L;

        when(sessionFactory.openSession()).thenReturn(sqlSession);
        when(sqlSession.getMapper(PartnersMapper.class)).thenReturn(partnersMapper);
        doThrow(new SqlSessionException("Бд не доступна")).when(partnersMapper).searchByUpidAndContractId(any());

        assertThrows(SqlSessionException.class, () -> service.searchByUpidAndContractId(upid, contractId));

        verify(sessionFactory).openSession();
        verify(sqlSession).getMapper(PartnersMapper.class);
        verify(sqlSession).close();
    }
}