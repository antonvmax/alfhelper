package ru.alfastrah.site.avto.ws.contact.signed.service.printform.id.partner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.alfastrah.site.avto.unicus.services.UnicusUsrService;
import ru.alfastrah.site.avto.ws.contact.signed.client.AgentInfoClient;
import ru.alfastrah.site.avto.ws.contact.signed.model.juridical.AgentInfoResponse;
import tops.unicus.usr.RContractAgents;

import java.util.Optional;

@Slf4j
@Service
public class AgentBlockPrintFormCheck {
    private final UnicusUsrService usrService;
    private final AgentInfoClient client;

    public AgentBlockPrintFormCheck(UnicusUsrService usrService,
                                    AgentInfoClient client) {
        this.usrService = usrService;
        this.client = client;
    }

    public boolean check(Long contractId) {
        Optional<Long> agentId = usrService.getContractAgents(contractId).stream()
                .map(RContractAgents::getAgentContractId).findFirst();
        if (agentId.isPresent()) {
            AgentInfoResponse agentInfo = client.getAgentInfo(agentId.get());
            return Boolean.TRUE.equals(agentInfo.getCheckAgent());
        }
        return false;
    }
}
