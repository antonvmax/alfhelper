package ru.alfastrah.site.avto.ws.contact.signed.service.client.hid;

import org.springframework.stereotype.Service;
import ru.alfastrah.interplat4.model.cdi2.FuzzySearch;
import ru.alfastrah.interplat4.model.cdi2.FuzzySearchResponse;
import ru.alfastrah.interplat4.model.cdi2.WAttribute;
import ru.alfastrah.interplat4.model.cdi2.WField;
import ru.alfastrah.interplat4.model.cdi2.WMatchedParty;
import ru.alfastrah.interplat4.model.cdi2.WParty;
import ru.alfastrah.site.avto.cdi.services.PartyWsService;
import ru.alfastrah.site.avto.cdi.util.PartyType;
import ru.alfastrah.site.avto.ws.contact.signed.service.client.info.ClientInfo;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FuzzySearchClientHidService {
    private final PartyWsService partyWsService;

    public FuzzySearchClientHidService(PartyWsService partyWsService) {
        this.partyWsService = partyWsService;
    }

    public Long searchHid(ClientInfo clientInfo, List<String> emails) {
        FuzzySearch request = new FuzzySearch();
        WParty wParty = new WParty();
        wParty.setType(PartyType.PHYSICAL.name());
        wParty.getField().addAll(List.of(
                createWField("surname", clientInfo.lastName()),
                createWField("name", clientInfo.firstName()),
                createWField("patronymic", clientInfo.middleName()),
                createWField("birthdate", clientInfo.birthDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
        ));
        WAttribute emailAttribute = new WAttribute();
        emailAttribute.setType("EMAIL");
        emails.forEach(email -> emailAttribute.getField().add(createWField("rawSource", email)));
        wParty.getAttribute().add(emailAttribute);

        request.setParty(wParty);
        request.setMaxCount(2);
        FuzzySearchResponse fuzzySearchResponse = partyWsService.fuzzySearch(request);
        return Optional.ofNullable(fuzzySearchResponse)
                .map(FuzzySearchResponse::getMatchedParty).orElse(new ArrayList<>())
                .stream().findFirst()
                .map(WMatchedParty::getParty)
                .map(WParty::getHid)
                .orElse(null);
    }

    private WField createWField(String fieldName, String fieldValue) {
        WField name = new WField();
        name.setName(fieldName);
        name.setValue(fieldValue);
        return name;
    }
}
