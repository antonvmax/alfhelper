package ru.alfastrah.site.avto.ws.contact.signed.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.alfastrah.site.avto.ws.contact.signed.db.model.EmailRecipientFio;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class FIOParserTest {
    private final FIOParser service = new FIOParser();

    private static final String EXCEPTION_NO_DATA_MESSAGE = "Недостаточно данных в запросе. Неверно указано ФИО";

    @Test
    void shouldThrowExceptionWhenBadFioFormat() {
        assertThatThrownBy(() -> service.parseFIO("Ирина")).hasMessageContaining(EXCEPTION_NO_DATA_MESSAGE);
    }

    @Test
    void shouldThrowExceptionWhenFioNull() {
        assertThatThrownBy(() -> service.parseFIO(null)).hasMessageContaining(EXCEPTION_NO_DATA_MESSAGE);
    }

    @Test
    void shouldThrowExceptionWhenFioEmpty() {
        assertThatThrownBy(() -> service.parseFIO("")).hasMessageContaining(EXCEPTION_NO_DATA_MESSAGE);
    }

    @Test
    void shouldParseWhenHasNotMiddleName() {
        assertDoesNotThrow(() -> service.parseFIO("Иванова Ирина"));
        EmailRecipientFio fio = service.parseFIO("Иванова Ирина");

        assertEquals("иванова", fio.getLastName());
        assertEquals("ирина", fio.getFirstName());
        assertNull(fio.getMiddleName());
    }

    @Test
    void shouldParse() {
        assertDoesNotThrow(() -> service.parseFIO("Иванова Ирина Алексеевна"));
        EmailRecipientFio fio = service.parseFIO("Иванова Ирина Алексеевна");

        assertEquals("иванова", fio.getLastName());
        assertEquals("ирина", fio.getFirstName());
        assertEquals("алексеевна", fio.getMiddleName());
    }
}