package ru.alfastrah.site.avto.ws.contact.signed.db;

import org.apache.ibatis.exceptions.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.alfastrah.site.avto.client.unicus.db.partner.ContractInfoClient;
import ru.alfastrah.site.avto.model.unicus.db.dto.contract.ContractInfoDto;
import ru.alfastrah.site.avto.unicus.services.UnicusUsrService;
import tops.unicus.usr.RSaleContract;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartnersDbTest {

    @Mock
    private UnicusUsrService usrService;

    @Mock
    private ContractInfoClient contractInfoClient;

    private PartnersDb partnersDb;

    @BeforeEach
    void setUp() {
        partnersDb = new PartnersDb(usrService, contractInfoClient);
    }

    @Test
    void shouldReturnContractInfoWhenContractIdExists() {
        BigInteger contractId = BigInteger.valueOf(12345L);
        RSaleContract expectedContract = new RSaleContract();

        when(usrService.getFullSaleContract(contractId.longValue())).thenReturn(expectedContract);

        RSaleContract result = partnersDb.getContractInfo(contractId);

        assertEquals(expectedContract, result);
        verify(usrService).getFullSaleContract(contractId.longValue());
    }

    @Test
    void shouldThrowExceptionWhenUsrServiceThrowsException() {
        BigInteger contractId = BigInteger.valueOf(12345L);

        when(usrService.getFullSaleContract(contractId.longValue()))
                .thenThrow(new PersistenceException("Database connection failed"));

        assertThatThrownBy(() -> partnersDb.getContractInfo(contractId))
                .isInstanceOf(PersistenceException.class)
                .hasMessage("Database connection failed");
    }

    @Test
    void shouldReturnContractInfoDtoWhenAdditionalKaskoInfoExists() {
        String upid = "test-upid";
        BigInteger contractId = BigInteger.valueOf(12345L);
        ContractInfoDto expectedDto = new ContractInfoDto();

        when(contractInfoClient.getAdditionalKaskoInfo(upid, contractId)).thenReturn(expectedDto);

        ContractInfoDto result = partnersDb.getAdditionalKaskoInfo(upid, contractId);

        assertEquals(expectedDto, result);
        verify(contractInfoClient).getAdditionalKaskoInfo(upid, contractId);
    }

    @Test
    void shouldReturnNullWhenGetAdditionalKaskoInfoThrowsException() {
        String upid = "test-upid";
        BigInteger contractId = BigInteger.valueOf(12345L);

        doThrow(new RuntimeException("Connection timeout"))
                .when(contractInfoClient).getAdditionalKaskoInfo(upid, contractId);

        ContractInfoDto result = partnersDb.getAdditionalKaskoInfo(upid, contractId);

        assertNull(result);
    }
}