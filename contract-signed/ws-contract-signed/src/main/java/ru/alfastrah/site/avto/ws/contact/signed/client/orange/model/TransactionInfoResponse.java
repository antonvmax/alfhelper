package ru.alfastrah.site.avto.ws.contact.signed.client.orange.model;

import java.util.List;

public class TransactionInfoResponse {
    private List<Transaction> transactionList;
    private List<Reverse> reverseList;
    private UserInfo userInfo;

    public List<Transaction> getTransactionList() {
        return transactionList;
    }

    public void setTransactionList(List<Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    public List<Reverse> getReverseList() {
        return reverseList;
    }

    public void setReverseList(List<Reverse> reverseList) {
        this.reverseList = reverseList;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }
}
