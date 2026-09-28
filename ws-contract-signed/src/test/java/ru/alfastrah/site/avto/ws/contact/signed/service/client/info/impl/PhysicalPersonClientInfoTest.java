package ru.alfastrah.site.avto.ws.contact.signed.service.client.info.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tops.unicus.subject.RPhysicalPerson;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PhysicalPersonClientInfoTest {

    private PhysicalPersonClientInfo underTest;
    private RPhysicalPerson person;

    @BeforeEach
    void setUp() {
        this.person = new RPhysicalPerson()
                .withFirstName("FirstName")
                .withLastName("LastName")
                .withMiddleName("MiddleName")
                .withBirthDate(LocalDate.now());
        underTest = new PhysicalPersonClientInfo(person);
    }

    @Test
    void shouldReturnLowerCaseDataFromRPhysicalPerson() {
        assertThat(underTest.firstName()).isEqualTo(person.getFirstName().toLowerCase());
        assertThat(underTest.lastName()).isEqualTo(person.getLastName().toLowerCase());
        assertThat(underTest.middleName()).isEqualTo(person.getMiddleName().toLowerCase());
        assertThat(underTest.birthDate()).isEqualTo(person.getBirthDate());
        assertThat(underTest.email()).isEmpty();
    }
}