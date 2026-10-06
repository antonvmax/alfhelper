package ru.alfastrah.site.avto.ws.contact.signed.controller.model;

import lombok.Getter;
import lombok.Setter;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.SendContractSignedResponse;

import java.util.Objects;

@Setter
@Getter
public class SendContractSignedSyncResponse extends SendContractSignedResponse {

    private String error;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        SendContractSignedSyncResponse that = (SendContractSignedSyncResponse) o;
        return Objects.equals(error, that.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), error);
    }
}
