package br.com.backend.cadus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class CadusApplication {

	public static void main(String[] args) {
		SpringApplication.run(CadusApplication.class, args);
	}

}
