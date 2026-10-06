package ru.alfastrah.site.avto.ws.contact.signed.utils;

import org.springframework.stereotype.Service;
import ru.alfastrah.site.avto.model.contract.signed.exception.NoPersonalDataException;
import ru.alfastrah.site.avto.ws.contact.signed.db.model.EmailRecipientFio;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FIOParser {
    public EmailRecipientFio parseFIO(String fio) {
        EmailRecipientFio emailRecipientFio = new EmailRecipientFio();
        List<String> fioList = Stream.of(Optional.ofNullable(fio).orElse("").split(" ")).collect(Collectors.toList());

        if (fioList.size() < 2) {
            throw new NoPersonalDataException("Недостаточно данных в запросе. Неверно указано ФИО");
        }

        Iterator<String> iterator = fioList.iterator();

        emailRecipientFio.setLastName(iterator.next().toLowerCase());
        emailRecipientFio.setFirstName(iterator.next().toLowerCase());

        if (iterator.hasNext()) {
            emailRecipientFio.setMiddleName(iterator.next().toLowerCase());
        }

        return emailRecipientFio;
    }
}
