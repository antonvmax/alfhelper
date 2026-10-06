package ru.alfastrah.site.avto.payment.internet.contract;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "ru.alfastrah.site.avto.payment.internet.contract")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
