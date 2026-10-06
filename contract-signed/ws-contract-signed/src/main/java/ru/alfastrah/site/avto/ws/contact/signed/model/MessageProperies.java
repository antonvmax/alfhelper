package ru.alfastrah.site.avto.ws.contact.signed.model;

import org.springframework.stereotype.Service;
import ru.alfastrah.site.avto.ws.contact.signed.db.model.EmailRecipientFio;

@Service
public class MessageProperies {
    private String sID;
    private String emailBody;
    private EmailRecipientFio fio;
    private String recipientEmail;

    public String getsID() {
        return sID;
    }

    public void setsID(String sID) {
        this.sID = sID;
    }

    public String getEmailBody() {
        return emailBody;
    }

    public void setEmailBody(String emailBody) {
        this.emailBody = emailBody;
    }

    public EmailRecipientFio getFio() {
        return fio;
    }

    public void setFio(EmailRecipientFio fio) {
        this.fio = fio;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }
}


