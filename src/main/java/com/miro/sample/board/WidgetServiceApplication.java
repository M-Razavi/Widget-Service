package com.miro.sample.board;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.miro.sample.board.repository")
public class WidgetServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WidgetServiceApplication.class, args);
    }
}
