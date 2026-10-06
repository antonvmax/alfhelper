package ru.alfastrah.site.avto.ws.contact.signed.service.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.alfastrah.interplat4.ws.rsa.kbm.RsaKBMExceptionFault;
import ru.alfastrah.site.avto.ws.contact.signed.client.CBLogClient;
import ru.alfastrah.site.avto.ws.contact.signed.db.model.SiteRecord;
import ru.alfastrah.site.avto.ws.contact.signed.service.client.info.ClientInfo;
import ru.alfastrah.site.avto.ws.contact.signed.service.client.info.impl.PhysicalPersonClientInfo;
import tops.unicus.subject.RPhysicalPerson;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@ExtendWith(MockitoExtension.class)
class CBLoggerBeanTest {

    @Mock
    private CBLogClient cbLogClient;
    @Mock
    private ObjectMapper objectMapper;
    private CBLoggerBean underTest;

    @BeforeEach
    void setUp() {
        underTest = new CBLoggerBean(cbLogClient, objectMapper);
    }

    @Test
    void shouldSendRequestWithoutClientHashWhenClientBirthDateIsNull() throws RsaKBMExceptionFault {
        RPhysicalPerson person = new RPhysicalPerson()
                .withFirstName("firstName")
                .withLastName("LastName")
                .withMiddleName("MiddleName");
        ClientInfo clientInfo = new PhysicalPersonClientInfo(person);

        underTest.logCurrentContractSignedRequest(clientInfo, "email");


        ArgumentCaptor<SiteRecord> siteRecordArgumentCaptor = ArgumentCaptor.forClass(SiteRecord.class);
        verify(cbLogClient, times(1))
                .logContractSignedRequest(siteRecordArgumentCaptor.capture());
        assertThat(siteRecordArgumentCaptor.getValue().getUserCode()).isNull();
    }

    @Test
    void shouldGenerateUserCodeWhenBirthDateExists() throws RsaKBMExceptionFault {
        RPhysicalPerson person = new RPhysicalPerson()
                .withFirstName("firstName")
                .withLastName("LastName")
                .withMiddleName("MiddleName")
                .withBirthDate(LocalDate.now());

        underTest.logCurrentContractSignedRequest(new PhysicalPersonClientInfo(person), "email");

        ArgumentCaptor<SiteRecord> siteRecordArgumentCaptor = ArgumentCaptor.forClass(SiteRecord.class);
        verify(cbLogClient, times(1))
                .logContractSignedRequest(siteRecordArgumentCaptor.capture());
        assertThat(siteRecordArgumentCaptor.getValue().getUserCode()).isNotNull();
    }
}