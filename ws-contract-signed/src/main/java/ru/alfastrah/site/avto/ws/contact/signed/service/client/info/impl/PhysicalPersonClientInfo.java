package ru.alfastrah.site.avto.ws.contact.signed.service.client.info.impl;

import org.apache.commons.lang3.StringUtils;
import ru.alfastrah.site.avto.ws.contact.signed.service.client.info.ClientInfo;
import tops.unicus.subject.RPhysicalPerson;

import java.time.LocalDate;
import java.util.Optional;

public class PhysicalPersonClientInfo implements ClientInfo {

    private final RPhysicalPerson person;
    private String phone = StringUtils.EMPTY;
    public PhysicalPersonClientInfo(RPhysicalPerson person) {
        this.person = person;
    }

    public String firstName() {
        return person.getFirstName().toLowerCase();
    }

    public String middleName() {
        return Optional.ofNullable(person.getMiddleName()).orElse("").toLowerCase();
    }

    public String lastName() {
        return person.getLastName().toLowerCase();
    }

    public LocalDate birthDate() {
        return person.getBirthDate();
    }

    public String email() {
        return StringUtils.EMPTY;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String phone() {
        return this.phone;
    }
}
