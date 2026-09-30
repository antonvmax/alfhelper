package ru.alfastrah.site.avto.ws.partners.interaction.dlo;

import ru.alfastrah.site.avto.ws.partners.interaction.parameters.PartnerContract;

public interface PartnersPakDLO {

    void saveUPID(String UPID, String callerCode);

    PartnerContract getContractId(String UPID);

    void linkActionToUpid(String upid, String calcId, Long contractId);

    Long searchByUpidAndContractId(String upid, Long contractId);
}
