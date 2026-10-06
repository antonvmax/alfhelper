package ru.alfastrah.site.avto.ws.contact.signed.service;

import java.util.Arrays;

public enum Product {
    OSAGO("OSAGO"),
    WHITE_CARD("190"),
    KASKO("046"),
    ALFA_REPAIR("354"),
    NS397("397"),
    NS398("398"),
    GOOD_NEIGHBORS("081"),
    PRODUCT_MORTGAGE_LIFE("118"),
    PRODUCT_MORTGAGE_PROPERTY("373"),
    KROSS_EOSAGO("354"),
    UNKNOWN("-1");

    private final String productId;

    Product(String productId) {
        this.productId = productId;
    }

    public String getProductId() {
        return this.productId;
    }

    public static Product fromId(String productId) {
        return Arrays.stream(values())
                .filter(product -> product.getProductId().equals(productId))
                .findFirst()
                .orElse(UNKNOWN);
    }
}
