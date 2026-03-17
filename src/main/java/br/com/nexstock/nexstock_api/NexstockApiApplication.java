package br.com.nexstock.nexstock_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NexstockApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(NexstockApiApplication.class, args);
	}

}
