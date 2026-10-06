package ru.alfastrah.site.avto.ws.partners.interaction.dlo;

import ru.alfastrah.site.avto.ws.partners.interaction.parameters.SaveUPIDParameters;

import java.util.Map;

public interface PartnersMapper {

    void getContractId(Map<String, Object> params);

    void saveUPID(SaveUPIDParameters parameters);

    void linkActionToUpid(Map<String, Object> params);

    Long searchByUpidAndContractId(Map<String, Object> params);
}
