package ru.alfastrah.site.avto.ws.contact.signed.service.client.info;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.ws.WebServiceException;
import ru.alfastrah.site.avto.model.contract.signed.exception.SendContractServerException;
import ru.alfastrah.site.avto.unicus.services.UnicusSubjectService;
import ru.alfastrah.site.avto.ws.contact.signed.service.client.info.impl.PhysicalPersonClientInfo;
import tops.unicus.subject.RJuridicalPerson;
import tops.unicus.subject.RPhysicalPerson;
import tops.unicus.usr.RSaleContract;

@Slf4j
@Service
public class ClientInfoFactory {
    private static String PHYSICAL_PERSON = "PHYSICAL_PERSON";
    private static String JURIDICAL_PERSON = "JURIDICAL_PERSON";
    private final UnicusSubjectService subjectService;

    public ClientInfoFactory(UnicusSubjectService subjectService) {
        this.subjectService = subjectService;
    }

    public ClientInfo byContract(RSaleContract contract) {
        String subjectTypeId = contract.getSubjectTypeId();
        Long subjectId = contract.getSubjectId();
        if (JURIDICAL_PERSON.equals(subjectTypeId)) {
            return this.byJuridicalSubjectId(subjectId);
        } else if (PHYSICAL_PERSON.equals(subjectTypeId)) {
            return this.bySubjectId(subjectId);
        }
        throw new SendContractServerException("Не найден тип субъекта договора.");
    }

    public PhysicalPersonClientInfo bySubjectId(Long clientSubjectId) {
        log.debug("Запрос на clientSubject.getPhysicalPerson. Параметры: subjectId={}", clientSubjectId);
        try {
            RPhysicalPerson subjectInfo = subjectService.getPhysicalPerson(clientSubjectId);
            log.debug("Ответ из clientSubject.getPhysicalPerson:{}", subjectInfo);
            return new PhysicalPersonClientInfo(subjectInfo);
        } catch (WebServiceException e) {
            throw new SendContractServerException(e.getMessage());
        }
    }

    private JuridicalPersonClientInfo byJuridicalSubjectId(Long clientSubjectId) {
        try {
            RJuridicalPerson juridicalPerson = subjectService.getJuridicalPerson(clientSubjectId);
            log.debug("Ответ из clientSubject.getJuridicalPerson:{}", juridicalPerson);
            return new JuridicalPersonClientInfo(juridicalPerson);
        } catch (WebServiceException e) {
            throw new SendContractServerException(e.getMessage());
        }
    }
}