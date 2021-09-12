package com.domain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class NodeDomainApplication {

    public static void main(String[] args) {
        SpringApplication.run(NodeDomainApplication.class, args);
    }

}
