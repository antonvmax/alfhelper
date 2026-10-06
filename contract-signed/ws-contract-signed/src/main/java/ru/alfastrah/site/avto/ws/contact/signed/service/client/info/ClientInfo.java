package ru.alfastrah.site.avto.ws.contact.signed.service.client.info;

import java.time.LocalDate;

public interface ClientInfo {
    String lastName();
    String firstName();
    String middleName();
    LocalDate birthDate();
    String email();
    void setPhone(String phone);
    String phone();
}
