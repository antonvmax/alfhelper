package ru.alfastrah.site.avto.ws.contact.signed.utils;

import org.springframework.stereotype.Service;
import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoProccessException;

@Service
public class CreateFault {

    public EOsagoProccessException getError(EOsagoProccessException ex) {
        StringBuilder message = new StringBuilder();
        if (ex.getMessage() == null) {
            message.append(ex.getClass().getSimpleName());
            if (ex.getStackTrace() != null && ex.getStackTrace().length > 1) {
                message.append(" at \n").append(ex.getStackTrace()[0]);
            }
        } else {
            message.append(ex.getMessage());
        }
        String code = ex.getCode();
        return  new EOsagoProccessException(message.toString(), code);
    }

}
