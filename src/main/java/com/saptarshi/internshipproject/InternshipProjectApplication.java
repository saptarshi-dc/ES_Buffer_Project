package com.saptarshi.internshipproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = "com.saptarshi.internshipproject")
@EnableScheduling
public class InternshipProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(InternshipProjectApplication.class, args);
	}

}

