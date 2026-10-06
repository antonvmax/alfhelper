package ru.alfastrah.site.avto.ws.contact.signed.service.client.info;

import org.apache.commons.lang3.StringUtils;
import tops.unicus.subject.RJuridicalPerson;

import java.time.LocalDate;

public class JuridicalPersonClientInfo implements ClientInfo {

    private final RJuridicalPerson juridicalPerson;
    private String phone = StringUtils.EMPTY;
    public JuridicalPersonClientInfo(RJuridicalPerson juridicalPerson) {
        this.juridicalPerson = juridicalPerson;
    }
    @Override
    public String lastName() {
        return StringUtils.EMPTY;
    }

    @Override
    public String firstName() {
        return juridicalPerson.getFirmShortName();
    }

    @Override
    public String middleName() {
        return StringUtils.EMPTY;
    }

    @Override
    public LocalDate birthDate() {
        return null;
    }

    @Override
    public String email() {
        return StringUtils.EMPTY;
    }

    @Override
    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String phone() {
        return this.phone;
    }
}
