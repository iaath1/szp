package com.stg.szp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableJpaAuditing
@SpringBootApplication
@EnableAsync 
public class SzpApplication {

	public static void main(String[] args) {
		SpringApplication.run(SzpApplication.class, args);
	}

}
