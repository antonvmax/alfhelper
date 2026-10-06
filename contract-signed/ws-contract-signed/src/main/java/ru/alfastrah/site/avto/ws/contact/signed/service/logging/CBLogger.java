package ru.alfastrah.site.avto.ws.contact.signed.service.logging;

import ru.alfastrah.interplat4.ws.rsa.kbm.RsaKBMExceptionFault;
import ru.alfastrah.site.avto.ws.contact.signed.service.client.info.ClientInfo;

public interface CBLogger {
    void logCurrentContractSignedRequest(ClientInfo client, String email) throws RsaKBMExceptionFault;
}
