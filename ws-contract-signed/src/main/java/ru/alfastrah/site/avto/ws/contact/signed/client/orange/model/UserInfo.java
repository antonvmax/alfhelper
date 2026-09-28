package ru.alfastrah.site.avto.ws.contact.signed.client.orange.model;

public class UserInfo {
    private String phone;
    private Integer balance;
    private Boolean isParticipant;
    private Verification phoneVerification;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getBalance() {
        return balance;
    }

    public void setBalance(Integer balance) {
        this.balance = balance;
    }

    public Boolean getParticipant() {
        return isParticipant;
    }

    public void setParticipant(Boolean participant) {
        isParticipant = participant;
    }

    public Verification getPhoneVerification() {
        return phoneVerification;
    }

    public void setPhoneVerification(Verification phoneVerification) {
        this.phoneVerification = phoneVerification;
    }
}
