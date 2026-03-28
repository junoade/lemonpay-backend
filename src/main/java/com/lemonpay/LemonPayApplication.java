package com.lemonpay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class LemonPayApplication {

	public static void main(String[] args) {
		SpringApplication.run(LemonPayApplication.class, args);
	}

}
