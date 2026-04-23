package br.com.nexstock.nexstock_api;

import br.com.nexstock.nexstock_api.config.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(StorageProperties.class)
public class NexstockApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(NexstockApiApplication.class, args);
	}

}
