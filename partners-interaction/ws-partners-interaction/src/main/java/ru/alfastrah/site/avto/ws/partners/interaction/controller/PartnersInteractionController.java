package ru.alfastrah.site.avto.ws.partners.interaction.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.alfastrah.interplat4.partners.interaction.PayedContractRequest;
import ru.alfastrah.interplat4.partners.interaction.PayedContractResponse;
import ru.alfastrah.interplat4.partners.interaction.UPIDRequest;
import ru.alfastrah.interplat4.partners.interaction.UPIDResponse;
import ru.alfastrah.site.avto.model.partners.interaction.dto.LinkActionRequest;
import ru.alfastrah.site.avto.model.partners.interaction.dto.SearchByUpidAndContractIdRequest;
import ru.alfastrah.site.avto.ws.partners.interaction.config.WebServiceConfig;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PartnersInteractionController {

    private final WebServiceConfig.PartnersInteraction partnersInteraction;

    @RequestMapping("/getUPID")
    @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE})
    public UPIDResponse getUPID(@RequestBody UPIDRequest parameters) {
        return partnersInteraction.getUPID(parameters);
    }

    @RequestMapping("/getContractId")
    @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE})
    public PayedContractResponse getContractId(@RequestBody PayedContractRequest parameters) {
        return partnersInteraction.getContractId(parameters);
    }

    @RequestMapping("/searchByUpidAndContractId")
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE})
    public Long searchByUpidAndContractIdRequest(@RequestBody SearchByUpidAndContractIdRequest request) {
        return partnersInteraction.searchByUpidAndContractId(request);
    }

    @PostMapping("/linkActionToUpid")
    public void linkToAction(@RequestBody LinkActionRequest request) {
        partnersInteraction.linkActionToUpid(request);
    }
}
