package ru.alfastrah.site.avto.ws.contact.signed.db.repository;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.alfastrah.site.avto.model.contract.signed.model.EmailSendLogDto;

import java.util.Optional;

@Repository
public interface EmailSendLogRepository extends CrudRepository<EmailSendLogDto, Long> {

    @Modifying
    @Query("DELETE FROM email_send_log e WHERE e.request_hash = :requestHash")
    void deleteByRequestHash(String requestHash);

    Optional<EmailSendLogDto> findByRequestHash(String requestHash);
}
