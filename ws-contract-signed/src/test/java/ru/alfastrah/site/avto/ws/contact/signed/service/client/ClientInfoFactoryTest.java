package ru.alfastrah.site.avto.ws.contact.signed.service.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ws.soap.SoapBodyException;
import ru.alfastrah.site.avto.model.contract.signed.exception.SendContractServerException;
import ru.alfastrah.site.avto.unicus.services.UnicusSubjectService;
import ru.alfastrah.site.avto.ws.contact.signed.service.client.info.ClientInfoFactory;
import ru.alfastrah.site.avto.ws.contact.signed.service.client.info.impl.PhysicalPersonClientInfo;
import tops.unicus.subject.RPhysicalPerson;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@ExtendWith(MockitoExtension.class)
class ClientInfoFactoryTest {

    @Mock
    private UnicusSubjectService subjectService;
    private ClientInfoFactory underTest;

    @BeforeEach
    void setUp() {
        underTest = new ClientInfoFactory(subjectService);
    }

    @Test
    void shouldCallSubjectServiceWhenIdGiven() {
        RPhysicalPerson person = new RPhysicalPerson()
                .withFirstName("FirstName")
                .withLastName("LastName")
                .withMiddleName("MiddleName")
                .withBirthDate(LocalDate.now());
        PhysicalPersonClientInfo expectedClientInfo = new PhysicalPersonClientInfo(person);
        when(subjectService.getPhysicalPerson(anyLong())).thenReturn(person);

        Long actualId = 12L;


        PhysicalPersonClientInfo actualClientInfo = underTest.bySubjectId(actualId);

        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        verify(subjectService, times(1)).getPhysicalPerson(idCaptor.capture());
        assertThat(actualId).isEqualTo(idCaptor.getValue());
        assertThat(actualClientInfo).usingRecursiveComparison()
                .isEqualTo(expectedClientInfo);
    }

    @Test
    void shouldReThrowExceptionWhenErrorOccurred() {
        when(subjectService.getPhysicalPerson(anyLong())).thenThrow(new SoapBodyException("exception"));

        assertThatThrownBy(() -> underTest.bySubjectId(123L))
                .isInstanceOf(SendContractServerException.class);
    }
}