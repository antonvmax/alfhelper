package ru.alfastrah.site.avto.ws.contact.signed.model.loyalty;

import java.util.Objects;

public class SearchClientRequest {
    private String email;

    public SearchClientRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SearchClientRequest that = (SearchClientRequest) o;
        return Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}
