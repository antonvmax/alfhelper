package ru.alfastrah.site.avto.payment.cheque;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.alfastrah.site.avto.payment.cheque.client.internet.contract.InternetContractFeignClient;
import ru.alfastrah.site.avto.payment.cheque.client.osago.replace.OsagoReplaceFeignClient;
import ru.alfastrah.site.avto.payment.cheque.client.payment.methods.PaymentMethodsFeignClient;

@SpringBootApplication(scanBasePackages = "ru.alfastrah.site.avto.payment.cheque")
@EnableFeignClients(clients = {OsagoReplaceFeignClient.class, InternetContractFeignClient.class, PaymentMethodsFeignClient.class})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}