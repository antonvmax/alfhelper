package ru.alfastrah.site.avto.ws.contact.signed.service.client.hid;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.alfastrah.interplat4.model.cdi2.FuzzySearch;
import ru.alfastrah.interplat4.model.cdi2.FuzzySearchResponse;
import ru.alfastrah.interplat4.model.cdi2.WMatchedParty;
import ru.alfastrah.interplat4.model.cdi2.WParty;
import ru.alfastrah.site.avto.cdi.services.PartyWsService;
import ru.alfastrah.site.avto.ws.contact.signed.service.client.info.ClientInfo;
import ru.alfastrah.site.avto.ws.contact.signed.service.client.info.impl.PhysicalPersonClientInfo;
import tops.unicus.subject.RPhysicalPerson;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FuzzySearchClientHidServiceTest {

    @Mock
    private PartyWsService partyWsService;

    private FuzzySearchClientHidService underTest;

    @BeforeEach
    void setUp() {
        underTest = new FuzzySearchClientHidService(partyWsService);
    }

    @Test
    void searchHid_shouldReturnHidWhenMatchedPartyExists() {
        ClientInfo clientInfo = createClientInfo("Ivanov", "Ivan", "Ivanovich", LocalDate.of(1990, 1, 1));
        List<String> emails = List.of("ivanov@example.com");
        Long expectedHid = 12345L;

        FuzzySearchResponse response = new FuzzySearchResponse();
        WMatchedParty matchedParty = new WMatchedParty();
        WParty party = new WParty();
        party.setHid(expectedHid);
        matchedParty.setParty(party);
        response.getMatchedParty().add(matchedParty);

        given(partyWsService.fuzzySearch(any(FuzzySearch.class))).willReturn(response);

        Long result = underTest.searchHid(clientInfo, emails);

        assertThat(result).isEqualTo(expectedHid);
        verify(partyWsService).fuzzySearch(any(FuzzySearch.class));
    }

    @Test
    void searchHid_shouldReturnNullWhenNoMatchedParty() {
        ClientInfo clientInfo = createClientInfo("Petrov", "Petr", "Petrovich", LocalDate.of(1985, 5, 5));
        List<String> emails = List.of("petrov@example.com");

        FuzzySearchResponse response = new FuzzySearchResponse();
        response.getMatchedParty().clear();

        given(partyWsService.fuzzySearch(any(FuzzySearch.class))).willReturn(response);

        Long result = underTest.searchHid(clientInfo, emails);

        assertThat(result).isNull();
    }

    @Test
    void searchHid_shouldReturnNullWhenResponseIsNull() {
        ClientInfo clientInfo = createClientInfo("Sidorov", "Sidor", "Sidorovich", LocalDate.of(1970, 3, 3));
        List<String> emails = List.of("sidorov@example.com");

        given(partyWsService.fuzzySearch(any(FuzzySearch.class))).willReturn(null);

        Long result = underTest.searchHid(clientInfo, emails);

        assertThat(result).isNull();
    }

    @Test
    void searchHid_shouldBuildRequestWithCorrectFields() {
        ClientInfo clientInfo = createClientInfo("Kuznetsov", "Alexey", "Sergeevich", LocalDate.of(1995, 12, 12));
        List<String> emails = List.of("kuznetsov@example.com", "alex@example.com");

        FuzzySearchResponse response = new FuzzySearchResponse();
        WMatchedParty matchedParty = new WMatchedParty();
        WParty party = new WParty();
        party.setHid(999L);
        matchedParty.setParty(party);
        response.getMatchedParty().add(matchedParty);

        given(partyWsService.fuzzySearch(any(FuzzySearch.class))).willReturn(response);

        Long result = underTest.searchHid(clientInfo, emails);

        assertThat(result).isEqualTo(999L);
        verify(partyWsService).fuzzySearch(any(FuzzySearch.class));
    }

    @Test
    void searchHid_shouldHandleEmptyEmailList() {
        ClientInfo clientInfo = createClientInfo("Novikov", "Dmitry", "Viktorovich", LocalDate.of(1988, 7, 7));
        List<String> emails = new ArrayList<>();

        FuzzySearchResponse response = new FuzzySearchResponse();
        WMatchedParty matchedParty = new WMatchedParty();
        WParty party = new WParty();
        party.setHid(777L);
        matchedParty.setParty(party);
        response.getMatchedParty().add(matchedParty);

        given(partyWsService.fuzzySearch(any(FuzzySearch.class))).willReturn(response);

        Long result = underTest.searchHid(clientInfo, emails);

        assertThat(result).isEqualTo(777L);
    }

    private ClientInfo createClientInfo(String lastName, String firstName, String middleName, LocalDate birthDate) {
        RPhysicalPerson person = new RPhysicalPerson();
        person.setLastName(lastName);
        person.setFirstName(firstName);
        person.setMiddleName(middleName);
        person.setBirthDate(birthDate);

        return new PhysicalPersonClientInfo(person);
    }
}