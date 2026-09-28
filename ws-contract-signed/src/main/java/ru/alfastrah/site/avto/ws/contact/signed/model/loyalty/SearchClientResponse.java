package ru.alfastrah.site.avto.ws.contact.signed.model.loyalty;


import java.util.List;

public class SearchClientResponse {

    private List<LoyaltyClient> clients;

    public List<LoyaltyClient> getClients() {
        return clients;
    }

    public void setClients(List<LoyaltyClient> clients) {
        this.clients = clients;
    }

}


