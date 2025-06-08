package com.neosoft.pijamasbakend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PijamasBakendApplication {

    public static void main(String[] args) {
        SpringApplication.run(PijamasBakendApplication.class, args);
    }

}
