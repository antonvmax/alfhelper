package ru.alfastrah.site.avto.ws.contact.signed.service.printform.id.partner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.alfastrah.site.avto.unicus.services.UnicusUsrService;
import ru.alfastrah.site.avto.ws.contact.signed.client.AgentInfoClient;
import ru.alfastrah.site.avto.ws.contact.signed.model.juridical.AgentInfoResponse;
import tops.unicus.usr.RContractAgents;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AgentBlockPrintFormCheckTest {

    @Mock
    private UnicusUsrService usrService;

    @Mock
    private AgentInfoClient client;

    @InjectMocks
    private AgentBlockPrintFormCheck underTest;

    @Test
    void shouldReturnFalseWhenNoAgents() {
        Long contractId = 123L;
        given(usrService.getContractAgents(contractId)).willReturn(Collections.emptyList());

        boolean result = underTest.check(contractId);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnTrueWhenAgentCheckAgentIsTrue() {
        Long contractId = 456L;
        Long agentId = 789L;
        RContractAgents agent = mock(RContractAgents.class);
        given(agent.getAgentContractId()).willReturn(agentId);
        List<RContractAgents> agents = Collections.singletonList(agent);
        given(usrService.getContractAgents(contractId)).willReturn(agents);
        AgentInfoResponse agentInfo = new AgentInfoResponse();
        agentInfo.setCheckAgent(true);
        given(client.getAgentInfo(agentId)).willReturn(agentInfo);

        boolean result = underTest.check(contractId);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenAgentCheckAgentIsFalse() {
        Long contractId = 456L;
        Long agentId = 789L;
        RContractAgents agent = mock(RContractAgents.class);
        given(agent.getAgentContractId()).willReturn(agentId);
        List<RContractAgents> agents = Collections.singletonList(agent);
        given(usrService.getContractAgents(contractId)).willReturn(agents);
        AgentInfoResponse agentInfo = new AgentInfoResponse();
        agentInfo.setCheckAgent(false);
        given(client.getAgentInfo(agentId)).willReturn(agentInfo);

        boolean result = underTest.check(contractId);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalseWhenAgentCheckAgentIsNull() {
        Long contractId = 456L;
        Long agentId = 789L;
        RContractAgents agent = mock(RContractAgents.class);
        given(agent.getAgentContractId()).willReturn(agentId);
        List<RContractAgents> agents = Collections.singletonList(agent);
        given(usrService.getContractAgents(contractId)).willReturn(agents);
        AgentInfoResponse agentInfo = new AgentInfoResponse();
        agentInfo.setCheckAgent(null);
        given(client.getAgentInfo(agentId)).willReturn(agentInfo);

        boolean result = underTest.check(contractId);

        assertThat(result).isFalse();
    }

    @Test
    void shouldUseFirstAgentWhenMultipleAgents() {
        Long contractId = 999L;
        Long firstAgentId = 100L;
        RContractAgents agent1 = mock(RContractAgents.class);
        given(agent1.getAgentContractId()).willReturn(firstAgentId);
        RContractAgents agent2 = mock(RContractAgents.class);
        List<RContractAgents> agents = Arrays.asList(agent1, agent2);
        given(usrService.getContractAgents(contractId)).willReturn(agents);
        AgentInfoResponse agentInfo = new AgentInfoResponse();
        agentInfo.setCheckAgent(true);
        given(client.getAgentInfo(firstAgentId)).willReturn(agentInfo);

        boolean result = underTest.check(contractId);

        assertThat(result).isTrue();
        verify(agent2, never()).getAgentContractId();
    }
}