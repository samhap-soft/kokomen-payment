package com.samhap.kokomen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class KokomenPaymentApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(KokomenPaymentApiApplication.class, args);
    }
}
