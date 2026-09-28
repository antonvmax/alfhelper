package ru.alfastrah.site.avto.ws.contact.signed.model.juridical;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class AgentInfoResponse {
    private String login;
    private Long departmentId;
    private Long managerId;
    private Long agentContractId;
    private Boolean checkAgent;


}
