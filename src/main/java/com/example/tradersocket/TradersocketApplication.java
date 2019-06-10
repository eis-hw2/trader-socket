package com.example.tradersocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TradersocketApplication {

	public static void main(String[] args) {
		SpringApplication.run(TradersocketApplication.class, args);
	}

}
